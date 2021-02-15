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

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

import akka.actor._
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import shapeless.{
	Id => _,
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.error.ActorTerminatedError
import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''WizardStage''' type defines an [[akka.actor.Actor]] which is
 * spawned by a [[com.github.osxhacker.foundation.models.core.akkax.Wizard]] as it
 * transitions through its managed stages.
 * 
 * Each '''WizardStage''' terminates when `stageComplete` is called, thus
 * indicating the [[akka.actor.Actor]] has completed its work.
 *
 * @author osxhacker
 *
 */
abstract class WizardStage[StartT] ()
	extends Actor
		with ErrorPolicyAware
		with Slf4jLogging
{
	/// Self Type Constraints
	this : FSM[_, _] =>


	/// Class Imports
	import syntax.either._


	/// Instance Properties
	protected def stageContext : StartT;


	override def onError (cause : Throwable) : State =
	{
		wizard () ! WizardStageErrorMessage (
			ActorTerminatedError (self, Option (cause))
			);

		return stop ();
	}


	/**
	 * The activatePriorStage method producdes a control
	 * [[com.github.osxhacker.foundation.models.core.akkax.WizardMessage]] which induces
	 * the managing [[com.github.osxhacker.foundation.models.core.akkax.Wizard]] to
	 * deactivate this '''WizardStage''' and '''reactivate''' the previous
	 * one (if any).
	 */
	def activatePriorStage (replay : Option[Any]) : Unit =
		wizard () ! WizardActivatePriorStageMessage (replay);


	/**
	 * The shutdown method allows a
	 * [[com.github.osxhacker.foundation.models.core.akkax.WizardStage]] to indicate to
	 * its controlling [[com.github.osxhacker.foundation.models.core.akkax.Wizard]] that
	 * an orderly shutdown is desired.
	 */
	def shutdown () : Unit =
		wizard () ! WizardShutdownMessage ();


	/**
	 * The stageComplete method indicates to the managing
	 * [[com.github.osxhacker.foundation.models.core.akkax.Wizard]] that this
	 * '''WizardStage''' has finished up and expects no more messages as of
	 * the invocation of `stageComplete`.
	 */
	def stageComplete[NextT, ReplyT] (state : NextT, reply : ReplyT)
		(implicit ev : WizardStage.CompletionContext[StartT, NextT])
		: Unit =
		wizard () ! WizardStageCompletedMessage (state :: reply :: HNil);


	/**
	 * The wizard method resolves the '''Wizard''' responsible for '''this'''
	 * '''WizardStage'''.
	 */
	protected def wizard () : ActorRef = context.parent;
}


object WizardStage
{
	/// Class Types
	@implicitNotFound (
		"stageComplete must be given the next context to use, which cannot be a '${A}'"
		)
	sealed trait CompletionContext[A, B]
	
	
	object CompletionContext
	{
		implicit def allowed[A, B] (implicit ev : A <:!< B)
			: CompletionContext[A, B] =
			new CompletionContext[A, B] {}
	}
}
