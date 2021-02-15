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

import akka.actor.ActorRef
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import shapeless.{
	Id => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''WizardMessage''' trait defines the common base for
 * [[com.github.osxhacker.foundation.models.core.akkax.Message]] control messages.  All
 * one-way messages understood by
 * [[com.github.osxhacker.foundation.models.core.akkax.Wizard]] instances are a sub-type
 * of '''WizardRequest'''.
 *
 * @author osxhacker
 *
 */
sealed trait WizardMessage[M <: WizardMessage[M]]
	extends Message[M]


/**
 * The '''WizardRequest''' trait defines the common base for
 * [[com.github.osxhacker.foundation.models.core.akkax.Request]] control messages.  All
 * messages understood by [[com.github.osxhacker.foundation.models.core.akkax.Wizard]]
 * instances are a sub-type of '''WizardRequest'''.
 *
 * @author osxhacker
 *
 */
sealed trait WizardRequest[R <: WizardResponse[R]]
	extends Request[WizardResponse[R]]
{
	/// Class Imports


	/// Class Types


	/// Instance Properties
	
}


/**
 * The '''WizardResponse''' trait defines the common base for
 * [[com.github.osxhacker.foundation.models.core.akkax.Response]] control messages.  All
 * messages produced by [[com.github.osxhacker.foundation.models.core.akkax.Wizard]]
 * instances are a sub-type of '''WizardResponse'''.
 *
 * @author osxhacker
 *
 */
sealed trait WizardResponse[R <: WizardResponse[R]]
	extends Response[WizardResponse[R]]
{
	/// Class Imports


	/// Class Types


	/// Instance Properties
	
}


final case class WizardActivatePriorStageMessage (
	val replay : Option[Any]
	)
	extends WizardMessage[WizardActivatePriorStageMessage]


object WizardActivatePriorStageMessage
{
	def apply (replay : Any) : WizardActivatePriorStageMessage =
		new WizardActivatePriorStageMessage (Some (replay));
}


final case class WizardCurrentStageRequest ()
	extends WizardRequest[WizardCurrentStageResponse]


final case class WizardCurrentStageResponse (
	val result : ApplicationError \/ Int
	)
	extends WizardResponse[WizardCurrentStageResponse]


final case class WizardLoadedMessage[A] (
	val result : ApplicationError \/ A
	)
	extends WizardMessage[WizardLoadedMessage[A]]


final case class WizardShutdownMessage ()
	extends WizardMessage[WizardShutdownMessage]


final case class WizardStageCompletedMessage[A, B] (
	val result : A :: B :: HNil
	)
	extends WizardMessage[WizardStageCompletedMessage[A, B]]


final case class WizardStageErrorMessage (failure : ApplicationError)
	extends WizardMessage[WizardStageErrorMessage]

