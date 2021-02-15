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
import scala.language.postfixOps
import scala.reflect.ClassTag

import akka.actor._
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import org.scalatest.{
	Assertions,
	WordSpecLike
	}

import shapeless._
import shapeless.ops.hlist._

import com.github.osxhacker.foundation.models.core.error._


/**
 * The '''WizardSpec''' type defines the certification suite for
 * [[com.github.osxhacker.foundation.models.core.akkax.Wizard]] types.
 *
 * @author osxhacker
 *
 */
final class WizardSpec
	extends ActorBasedSpec ("test-wizard")
		with WordSpecLike
		with Assertions
{
	/// Class Imports
	import scalaz.syntax.all._


	/// Class Types
	type Stages = StageOne :: StageTwo :: StageThree :: HNil


	case class StageOne (val n : Int)
	

	case class StageTwo (val message : String)
	
	
	case class StageThree (val n : Int, val message : String)
	
	
	case object GoBack
	
	
	case object SkipToStageThree


	case object TriggerStageCompletion


	class Worker[A : ClassTag] (override protected val stageContext : A)
		extends WizardStage[A]
			with FSM[Worker.State, Worker.Context]
	{
		/// Class Imports
		import Worker._


		/// Constructor Body
		debug (
			"#### started worker path={} A={}",
			self.path,
			implicitly[ClassTag[A]].runtimeClass.getName
			);


		startWith (Running, Uninitialized);

		when (Running) {
			case Event (GoBack, _) =>
				debug ("#### activating prior stage worker={}", self.path);
				activatePriorStage (replay = None);
				stay ();

			case Event (SkipToStageThree, _) =>
				debug ("#### skipping to stage three state={}", stageContext);

				stageComplete (
					StageThree (99, "skipped!"),
					StageThree (99, "skipped!")
					);

				stay ();

			case Event (TriggerStageCompletion, _) =>
				debug ("#### triggering completion state={}", stageContext);

				stageComplete (
					StageTwo ("from worker"),
					StageTwo ("from worker")
					);

				stay ();

			case Event (e : ApplicationError, _) =>
				debug ("### triggering failure error={}", e);
				onError (e);

			case Event (msg, _) =>
				debug ("#### worker message={} context={}", msg, stageContext);
				sender () ! stageContext;
				stay ();
			}


		override def postStop () : Unit =
			debug (
				"### stopped worker path={} A={}",
				self.path,
				implicitly[ClassTag[A]].runtimeClass.getName
				);
	}


	object Worker
	{
		/// Class Types
		sealed trait State


		case object Running
			extends State


		sealed trait Context


		case object Uninitialized
			extends Context
	}


	class SampleWizard ()
		extends Wizard[Stages]
	{
		/// Class Imports
		import SampleWizard._


		/// Instance Properties
		override val activators : List[Wizard.StageActivatorLike] =
			liftStages ().unify.toList;
		
		
		override def preStart () : Unit =
		{
			super.preStart ();

			loaded (StageOne (42));
		}
	}
	
	
	object SampleWizard
	{
		/// Class Imports
		import Wizard.StageActivator


		implicit def workerActivator[A] (implicit ct : ClassTag[A])
			: StageActivator[A] =
			new StageActivator[A] {
				override protected val classTag = ct;
				
				override def activate (a : A)
					(implicit context : ActorContext)
					: ActorRef =
					context.actorOf (Props (new Worker[A] (a)));
				}
	}


	"A Wizard" must {
		"be able to load" in {
			val wizard = system.actorOf (Props (new SampleWizard ()));

			wizard ! "what say ye?";

			val response = expectMsgType[StageOne] (3 seconds);
			
			assert (response.n === 42);
			}

		"be able to transition forward" in {
			val wizard = system.actorOf (Props (new SampleWizard ()));

			assertStageIs (wizard, 0);
			wizard ! "simulating doing stuff...";
			expectMsgType[StageOne] (3 seconds);
			
			wizard ! TriggerStageCompletion;

			val response = expectMsgType[StageTwo] (1 second);

			assertStageIs (wizard, 1);
			assert (!response.message.isEmpty);

			wizard ! "in stage 2";

			val second = expectMsgType[StageTwo] (1 second);

			assert (!second.message.isEmpty);
			assertStageIs (wizard, 1);
			}

		"be able to transition backward" in {
			val wizard = system.actorOf (Props (new SampleWizard ()));

			assertStageIs (wizard, 0);
			wizard ! TriggerStageCompletion;

			val response = expectMsgType[StageTwo] (1 second);

			assert (!response.message.isEmpty);

			wizard ! GoBack;
			wizard ! "reply with stage context";

			expectMsgType[StageOne] (1 second);
			assertStageIs (wizard, 0);
			}

		"be able to 'skip' to a specific stage" in {
			val wizard = system.actorOf (Props (new SampleWizard ()));

			wizard ! "should be in stage one";

			val response = expectMsgType[StageOne] (3 seconds);

			assert (response.n === 42);

			wizard ! SkipToStageThree;

			expectMsgType[StageThree] (1 second);

			assertStageIs (wizard, 2);
			}

		"return to the stage which initiated a 'skip'" in {
			val wizard = system.actorOf (Props (new SampleWizard ()));

			wizard ! "should be in stage one";

			val response = expectMsgType[StageOne] (3 seconds);

			assert (response.n === 42);

			wizard ! SkipToStageThree;

			expectMsgType[StageThree] (1 second);

			assertStageIs (wizard, 2);
			wizard ! GoBack;
			wizard ! "reply with stage context";

			expectMsgType[StageOne] (1 second);
			assertStageIs (wizard, 0);
			}

		"propagate errors" in {
			val wizard = system.actorOf (Props (new SampleWizard ()));

			watch (wizard);
			wizard ! DomainValueError ("simulated");
			expectMsgType[Status.Failure] (1 second);
			expectTerminated (wizard);
			}
		}
	
	
	private def assertStageIs (wizard : ActorRef, expected : Int) : Unit =
	{
		wizard ! WizardCurrentStageRequest ();
		
		val result = expectMsgType[WizardCurrentStageResponse] (1 second);

		assert (
			result.result.exists (_ === expected),
			s"expected wizard to be in stage ${expected}, but was ${result}"
			);
	}
}
