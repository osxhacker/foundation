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
import scala.language.postfixOps
import scala.util.Random

import akka.actor._
import akka.pattern.pipe
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Identifier
import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.akkax._
import com.github.osxhacker.foundation.models.core.akkax.error._
import com.github.osxhacker.foundation.models.core.entity.EntityRef
import com.github.osxhacker.foundation.models.core.error._
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.net.URN
import com.github.osxhacker.foundation.models.core.text.Explanation
import com.github.osxhacker.foundation.models.notification.NotificationSettings
import com.github.osxhacker.foundation.models.notification.email._
import com.github.osxhacker.foundation.models.notification.error.MaximumRetriesExceededError
import com.github.osxhacker.foundation.models.notification.repository.EmailRepository


/**
 * The '''SendEmail''' type defines the Domain Object Model representation of
 * the asynchronous ability to deliver an
 * [[com.github.osxhacker.foundation.models.notification.email.Email]] using a
 * network accessible
 * [[https://en.wikipedia.org/wiki/Message_transfer_agent MTA]].
 *
 * @author osxhacker
 *
 */
final class SendEmail (private val emailRef : EntityRef[Email])
	(implicit emailRepository : EmailRepository, ec : ExecutionContext)
	extends FSM[SendEmail.State, SendEmail.Context]
		with ErrorPolicyAware
		with LocalMaterializer
		with MonitorLike
		with Slf4jLogging
		with StopOnErrorPolicy
		with SendEmailOperations
{
	/// Class Imports
	import SendEmail._
	import akkax.streams._
	import functional.futures.monad._
	import functional.kestrel._
	import std.option._
	import syntax.all._
	import syntax.std.option._


	/// Instance Properties
	val settings = NotificationSettings (context.system);

	override val retries = settings retries;
	override val timeRange = settings timeRange;


	/// Constructor Body
	startWith (Initializing, Uninitialized);


	/// The Initializing state is entered into when the actor must load (or
	/// refresh) the `Email` entity.  Once it is loaded, the ''Loaded''
	/// state is activated.
	when (Initializing, stateTimeout = settings.loadingTimeout) {
		case Event (StateTimeout, _) =>
			error (
				"unable to initialize SendEmail actor={} email={}",
				self.path,
				emailRef
				);

			onError (TimeoutError ("loading email"));

		case Event (\/- (Email.IsPending (instance : Email)), _) =>
			debug ("loaded email entity={}", instance);

			canBeTransmitted (instance, settings) {
				email =>

				markAs (email.transmitting ())
					.run
					.pipeTo (self);

				goto (Saving) using (NextState (Delivering));
				} | onError (
					MaximumRetriesExceededError (
						"giving up delivering email",
						settings.email.maximumAttempts
						)
					);

		case Event (\/- (Email.IsComplete (instance : Email)), _) =>
			debug ("email delivery is complete entity={}", instance);
			stop ();

		case Event (\/- (instance : Email), _) =>
			debug ("delaying email delivery entity={}", instance);

			goto (DelayedRetry)
				.forMax (randomDelay (settings.errorTimeout))
				.using (
					CanRetry (
						instance.isRetryable (settings.email.maximumAttempts)
						)
					);

		case Event (-\/ (failure : ApplicationError), _) =>
			error (
				"failed to initialize SendEmail actor={} email={} error={}",
				self.path,
				emailRef,
				failure
				);

			onError (failure);
		}


	/// The Delivering state is enterd into when the `FSM` has successfully
	/// hydrated, marked the `Email` as starting to be transmitted, and
	/// initiated the `DeliveryTask`.
	when (Delivering, stateTimeout = settings.requestTimeout) {
		case Event (StateTimeout, _) =>
			error (
				"timed out trying to start delivery actor={} email={}",
				self.path,
				emailRef
				);

			onError (TimeoutError ("start delivery"));

		case Event (\/- (receipt : Receipt), Latest (email)) =>
			debug (
				"successfully sent email actor={} email={} result={}",
				self.path,
				emailRef,
				receipt
				);

			complete (email, receipt).run
				.pipeTo (self);

			goto (Saving) using (NextState (Finished));

		case Event (-\/ (failure : ApplicationError), Latest (email)) =>
			error (
				"failed to start delivery actor={} email={} error={}",
				self.path,
				emailRef,
				failure
				);

			markAs (email.reject (deliveryFailed)).run
				.pipeTo (self);

			goto (Saving)
				.forMax (randomDelay (settings.errorTimeout))
				.using (
					CanRetry (
						email.isRetryable (settings.email.maximumAttempts)
						)
					);
		}


	/// The DelayedRetry state is entered when the `Email` cannot be sent.
	/// This state will remain active for `errorTimeout` and either `retry`
	/// or give up.  Note that the state timeout *must* be provided in the
	/// `goto` which activates this state.
	when (DelayedRetry) {
		case Event (StateTimeout, CanRetry (true)) =>
			debug (
				"retrying email delivery actor={} email={}",
				self.path,
				emailRef
				);

			retryWorkflow (emailRef, settings).run
				.pipeTo (self);

			goto (Saving) using (NextState (Initializing));

		case Event (StateTimeout, _) =>
			info (
				"giving up email delivery actor={} email={}",
				self.path,
				emailRef
				);

			stop ();
		}


	/// The Saving state is entered when '''SendEmail''' has initiated a `save`
	/// repository operation and is awaiting the result.  Since it can be
	/// entered into from multiple `State`s, its `Context` is expected to be the
	/// `NextState` type.
	when (Saving, stateTimeout = settings.requestTimeout) {
		case Event (StateTimeout, _) =>
			error (
				"timed out trying to save actor={} email={}",
				self.path,
				emailRef
				);

			onError (TimeoutError ("saving"));

		case Event (\/- (email : Email), NextState (desired)) =>
			goto (desired) using (Latest (email));

		case Event (-\/ (failure : ApplicationError), _) =>
			error (
				"failed to save actor={} email={} error={}",
				self.path,
				emailRef,
				failure
				);

			onError (failure);
		}


	/// The Finished state exists so that once Saving completes, it has a
	/// valid state to transition into.
	when (Finished, stateTimeout = settings.requestTimeout) {
		case Event (StateTimeout, _) =>
			onError (LogicError ("transitioning into Finished"));
		}


	onTransition {
		case _ -> Finished =>
			context stop (self);

		case _ -> Initializing =>
			refresh (self, emailRef);

		case _ -> Delivering =>
			nextStateData match {
				case Latest (email) =>
					startTransmission (self, email, settings).swap
						.foreach (failure => self ! -\/ (failure));

				case other =>
					error ("unexpected SendEmail FSM context: {}", other);
					context stop (self);
			}
		}


	initialize ();


	private def randomDelay (near : Duration) : Duration =
		near * (0.75 + Random.nextDouble ()).max (1.25);
}


object SendEmail
{
	/// Class Types
	sealed trait State


	case object DelayedRetry
		extends State


	case object Errored
		extends State


	case object Finished
		extends State


	case object Initializing
		extends State


	case object Saving
		extends State


	case object Delivering
		extends State


	sealed trait Context


	case object Uninitialized
		extends Context


	case class CanRetry (val yes : Boolean)
		extends Context


	case class Latest (val email : Email)
		extends Context


	case class NextState[S <: State] (val state : S)
		extends Context


	/// Instance Properties
	private val deliveryFailed = Explanation (
		Identifier (new URN (Explanation.scheme, "email-delivery-failed"))
		);


	/**
	 * The apply method is provided so that the details of how to instantiate a
	 * '''SendEmail''' instance is insulated from clients.
	 */
	def apply (email : EntityRef[Email])
		(
			implicit factory : ActorRefFactory,
			emailRepository : EmailRepository,
			ec : ExecutionContext
		)
		: ActorRef =
		factory.actorOf (Props (new SendEmail (email)));
}
