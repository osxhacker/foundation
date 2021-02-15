/**
 * MIT License
 * 
 * Copyright (c) 2021 osxhacker
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

package com.github.osxhacker.foundation.models.core.akkax

import scala.concurrent.duration._
import scala.language.{
	higherKinds,
	postfixOps
	}

import scala.reflect.ClassTag

import scalaz.{
	Failure => _,
	Id => _,
	Success => _,
	_
	}

import akka.actor._
import shapeless.{
	syntax => _,
	Zipper => _,
	_
	}

import shapeless.ops.hlist._

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	LogicError
	}


/**
 * =Overview=
 * 
 * The '''Wizard''' type defines a generic [[akka.actor.Actor]] which
 * provides transition management of distinct ''StagesT'' as well as serving in
 * a supervisory capacity for any child [[akka.actor.Actor]]s.
 *
 * =Implementation Notes=
 * 
 * Each [[com.github.osxhacker.foundation.models.core.akkax.WizardStage]] is activated
 * when first entered and remains active in the stage "history stack."  Due to
 * this, when prior stage navigation is desired, stages which support that
 * __must__ be able to resume strictly from either being active (which it will
 * not know until it receives a message) ''or'' by the stage activating it
 * providing a message for the prior stage to process.
 * 
 * The __current__ [[com.github.osxhacker.foundation.models.core.akkax.WizardStage]] is
 * stopped when activating its prior stage however.  This allows subsequent
 * stage activation to reinitialize based on what the reactivation of the prior
 * one provides.
 * 
 * @author osxhacker
 *
 */
abstract class Wizard[StagesT <: HList] ()
	extends FSM[Wizard.State, Wizard.Context]
		with Stash
		with Slf4jLogging
{
	/// Class Imports
	import SupervisorStrategy._
	import std.list._
	import std.option._
	import syntax.all._
	import syntax.std.all._
	import Wizard._


	/// Instance Properties
	final override val supervisorStrategy = OneForOneStrategy () {
		case ae : ApplicationError =>
			self ! WizardStageErrorMessage (ae);
			Stop;
		}

	/**
	 * The activators property is delegated to the concrate sub-type so that
	 * each ''StageActivator'' can capture whatever collaborators each requires
	 * for use when they are `activate`d.
	 */
	def activators : List[StageActivatorLike];
	

	/// Constructor Body
	startWith (Loading, Uninitialized);


	/// The Loading state is the first one entered when the Wizard starts.  It
	/// awaits the concrete type's completion of its initialization as
	/// indicated by reception of a WizardLoaded message.
	when (Loading) {
		case Event (WizardLoadedMessage (\/- (message)), _) =>
			debug (
				"loaded initial context wizard={} message={}",
				self.path,
				message
				);

			val starting = for {
				asZipper <- activators.toZipper
				loc <- asZipper.findZ (_.isDefinedAt (message))
				} yield loc;

			nextOrErrored (starting \/> UnknownStage) {
				zipper =>

				val stage = zipper.focus (message) (context);

				goto (Idle) using Environment (
					stage,
					zipper
					);
				}

		case Event (WizardLoadedMessage (-\/ (failure)), _) =>
			error ("failed loading wizard={} error={}", self.path, failure);
			goto (Errored) forMax (1 second) using Problem (failure);

		case Event (_ : WizardRequest[_], _) =>
			stash ();
			stay ();
		}
	
	
	/// The Idle state is entered when the Wizard has completed initialization
	/// and is not currently servicing a client's request.  Any messages which
	/// are not Wizard "control" types are sent to the `stage`.
	when (Idle) {
		case Event (_ : WizardCurrentStageRequest, env : Environment) =>
			sender () ! WizardCurrentStageResponse (env.current.right);
			stay ();

		case Event (WizardStageErrorMessage (failure), _) =>
			error (
				"stage failure detected wizard={} error={}",
				self.path,
				failure
				);

			goto (Errored) forMax (1 second) using (Problem (failure));

		case Event (WizardShutdownMessage (), _) =>
			error (
				"received shutdown message while Idle wizard={}",
				self.path
				);

			goto (Errored) forMax (1 second) using (
				Problem (LogicError ("received shutdown message while Idle"))
				);

		/// Any other control messages represent a logic error.  As such, they
		/// logged, the sender given a Status.Failure, and the Wizard stops.
		case Event (
			control : WizardRequest[_],
			Environment (stage, _, _, _)
			) =>
			error (
				"unexpected wizard control message received wizard={} stage={} message={} context={}",
				self.path,
				stage,
				control
				);

			sender () ! Status.Failure (
				LogicError ("unexpected control message")
				);

			stop ();

		case Event (message, env : Environment) =>
			debug (
				"wizard forwarding to stage wizard={} stage={} message={}",
				self.path,
				env.activeStage.path,
				message
				);

			env.activeStage ! message;
			
			goto (Busy) using env.withClient (sender ());
		}


	/// The Busy state is entered into when the active stage has received a
	/// message from a client and has yet to produce a response.
	when (Busy) {
		case Event (
			WizardActivatePriorStageMessage (replay),
			env : Environment
			) =>
			env.client.zip (replay) foreach {
				case (originator, message) =>

				/// If we are given a message to replay, simulate it coming
				/// from the client which originated the initial message.
				self.tell (message, originator);
				}

			context stop (env.activeStage);
			nextStageOrStop (env.prior);

		case Event (
			WizardStageCompletedMessage (latest :: reply :: HNil),
			env : Environment
			) =>
			env.client foreach (_ ! reply);
			nextStageOrStop (env.next (latest));

		case Event (WizardStageErrorMessage (failure), env : Environment) =>
			error (
				"failed completing stage wizard={} stage={} error={}",
				self.path,
				env.activeStage.path,
				failure
				);

			/// Since we do not know the precise response type the client is
			/// expecting to indicate failure, we'll fail the request instead.
			env.client foreach (_ ! Status.Failure (failure));
			context stop (env.activeStage);
			goto (Errored) forMax (1 second) using Problem (failure);

		case Event (WizardShutdownMessage (), _) =>
			debug (
				"shutting down wizard={} requestor={}",
				self.path,
				sender ().path
				);

			stop ();

		/// Any other message produced by the `activeStage` will be sent to the
		/// `env.client` and transitions the Wizard back to being Idle.
		case Event (response, env : Environment)
			if (sender () == env.activeStage) =>
			debug (
				"wizard producing response stage={} response={} receiver={}",
				env.activeStage.path,
				response,
				env.client map (_.path)
				);

			env.client foreach (_ ! response);
			goto (Idle) using env.withoutClient ();
		}
	
	
	when (Errored) {
		case Event (StateTimeout, _) =>
			stop ();

		case Event (_, Problem (failure)) =>
			sender () ! Status.Failure (failure);
			stay ();
		}


	whenUnhandled {
		case Event (unhandled : WizardRequest[_], _) =>
			error (
				"wizard control message not handled wizard={} state={} message={}",
				self.path,
				stateName,
				unhandled
				);
			
			stop ();

		case Event (msg, _) =>
			debug (
				"delaying wizard message wizard={} message={}",
				self.path,
				msg
				);

			stash ();
			stay ();
		}


	onTransition {
		case _ -> Idle =>
			unstashAll ();

		case prior -> Errored =>
			error (
				"entering errored state wizard={} from={} environment={}",
				self.path,
				prior,
				stateData
				);

			unstashAll ();
		}


	initialize ();
	
	
	/**
	 * The liftStages method is provided to make the definition of `activators`
	 * as easy as possible.
	 */
	protected def liftStages ()
		(implicit lifted : LiftAll[StageActivator, StagesT])
		: lifted.Out =
		lifted.instances;
	
	
	protected def loaded (first : Any) : Unit =
		self ! WizardLoadedMessage (\/- (first));


	protected def loadingFailed (problem : ApplicationError) : Unit =
		self ! WizardLoadedMessage (-\/ (problem));


	private def nextOrErrored[A] (a : ApplicationError \/ A)
		(f : A => State)
		: State =
		a.fold (
			e => goto (Errored) forMax (1 second) using Problem (e),
			f
			);
	
	
	private def nextStageOrStop (env : Option[Environment]) : State =
		env.cata (
			latest => goto (Idle) using latest,
			stop ()
			);
}


object Wizard
{
	/// Class Types
	sealed trait State

	case object Errored
		extends State

	case object Idle
		extends State

	case object Busy
		extends State

	case object Loading
		extends State


	sealed trait Context


	case object Uninitialized
		extends Context


	case class Environment (
		val activeStage : ActorRef,
		val zipper : Zipper[StageActivatorLike],
		val client : Option[ActorRef] = None,
		val prior : Option[Environment] = None
		)
		extends Context
	{
		/// Instance Properties
		val current : Int = zipper.index;


		def next (state : Any)
			(implicit context : ActorContext)
			: Option[Environment] =
			zipper.findNext (_.isDefinedAt (state)) map {
				z =>

				Environment (
					z.focus (state) (context),
					z,
					client = None,
					prior = Some (this)
					);
				}


		def withClient (requestor : ActorRef) : Environment =
			copy (client = Some (requestor));


		def withoutClient () : Environment = copy (client = None);
	}

	case class Problem (failure : ApplicationError)
		extends Context


	/**
	 * The '''StageActivatorLike''' type unifies generic
	 * [[com.github.osxhacker.foundation.models.core.akkax.Wizard.StageActivator]] types
	 * so that definitions can be in terms of
	 * [[com.github.osxhacker.foundation.models.core.akkax.Wizard.StageActivator]] while
	 * [[com.github.osxhacker.foundation.models.core.akkax.Wizard]] logic can be insulated
	 * from arbitrary (type-safe) environment types.
	 *
	 * @author osxhacker
	 *
	 */
	trait StageActivatorLike
		extends PartialFunction[Any, ActorContext => ActorRef]
	{
	}


	/**
	 * The '''StageActivator''' type is a specialized functor which can create
	 * an [[akka.actor.ActorRef]] as a child of the given '''context'''
	 * from an arbitrary ''A'' stage-specific initial environment.
	 *
	 * @author osxhacker
	 *
	 */
	abstract class StageActivator[A]
		extends StageActivatorLike
	{
		/// Instance Properties
		protected def classTag : ClassTag[A];


		override def apply (obj : Any) : ActorContext => ActorRef =
			context => activate (obj.asInstanceOf[A]) (context);


		override def isDefinedAt (obj : Any) : Boolean =
			classTag.runtimeClass.isAssignableFrom (
				obj.asInstanceOf[AnyRef].getClass
				);


		def activate (a : A)
			(implicit context : ActorContext)
			: ActorRef;
	}
	
	
	/// Instance Properties
	private val UnknownStage : ApplicationError =
		LogicError ("unknown wizard stage");
}

