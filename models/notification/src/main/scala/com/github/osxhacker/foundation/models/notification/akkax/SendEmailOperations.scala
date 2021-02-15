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

package com.github.osxhacker.foundation.models.notification.akkax

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.NotUsed
import akka.actor._
import akka.pattern.pipe
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.akkax._
import com.github.osxhacker.foundation.models.core.entity.EntityRef
import com.github.osxhacker.foundation.models.core.error._
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.notification.NotificationSettings
import com.github.osxhacker.foundation.models.notification.email._
import com.github.osxhacker.foundation.models.notification.repository.EmailRepository


/**
 * The '''SendEmailOperations''' trait defines the Domain Object Model behaviour
 * available to [[com.github.osxhacker.foundation.models.notification.akkax.SendEmail]].
 *
 * @author osxhacker
 *
 */
trait SendEmailOperations
{
	/// Self Type Constraints
	this : MaterializerAware =>


	/// Class Imports
	import akkax.streams._
	import functional.ErrorOr
	import functional.futures._
	import functional.futures.monad._
	import std.option._
	import syntax.std.boolean._
	import syntax.all._
	import syntax.std.option._


	/**
	 * The canBeTransmitted method produces an `Option[A]` when the '''email'''
	 * `isPending` and the prior number of attempts to send are below the
	 * `email.maximumAttempts` in the '''settings''' given.
	 */
	def canBeTransmitted[A] (email : Email, settings : NotificationSettings)
		(f : Email => A)
		: Option[A] =
		email.isPending ().option (email) >>= {
			e =>

			val maximum = settings.email.maximumAttempts;

			e.whenAttemptsAllow[Option, A] (maximum) (f (e));
			}


	/**
	 * The complete method marks the '''email''' as having been `sent`.
	 */
	def complete (email : Email, receipt : Receipt)
		(implicit emailRepository : EmailRepository)
		: FutureEither[Email] =
		markAs (email.sent (receipt));


	/**
	 * The markAs uses a deferred functor '''f''' to `save` if '''f''' is
	 * successful.
	 */
	def markAs (f : => Email#ValidatedChange[Email])
		(implicit emailRepository : EmailRepository)
		: FutureEither[Email] =
		Source.single (f)
			.valueOrFail ()
			.via (emailRepository.save ())
			.toFutureEither;


	/**
	 * The refresh method produces a
	 * [[com.github.osxhacker.foundation.models.core.functional.FutureEither]] having the
	 * latest [[com.github.osxhacker.foundation.models.notification.email.Email]]
	 * instance.
	 */
	def refresh (email : EntityRef[Email])
		(implicit emailRepository : EmailRepository)
		: FutureEither[Email] =
		emailRepository.find (email)
			.toFutureEither;


	/**
	 * This version of refresh automatically sends the ''\/'' result to the
	 * given '''requestor''' when ready.
	 */
	def refresh (requestor : ActorRef, email : EntityRef[Email])
		(implicit emailRepository : EmailRepository, ec : ExecutionContext)
		: Unit =
		refresh (email).run
			.pipeTo (requestor);


	/**
	 * The retryWorkflow method attempts to transition an '''email''' to a
	 * state which allows for retyring the delivery workflow.
	 */
	def retryWorkflow (
		email : EntityRef[Email],
		settings : NotificationSettings
		)
		(implicit emailRepository : EmailRepository, ec : ExecutionContext)
		: FutureEither[Email] =
		for {
			latest <- refresh (email)
			allowable <- FutureEither {
				latest.ensure (
					InvalidModelStateError ("maximum attempts exceeded")
					) {
					_.isRetryable (settings.email.maximumAttempts);
					}
				}

			saved <- markAs (allowable.retry ())
			} yield saved;


	/**
	 * The startTransmission method initiates the delivery of an '''email'''
	 * asynchronously.
	 */
	def startTransmission (
		requestor : ActorRef,
		email : Email,
		settings : NotificationSettings
		)
		(implicit factory : ActorRefFactory, ec : ExecutionContext)
		: ApplicationError \/ ActorRef =
		email.createEnvelope () map {
			envelope =>

			DeliveryTask (requestor, envelope, settings);
			}
}
