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

package com.github.osxhacker.foundation.models.security.akkax

import scala.language.postfixOps

import akka.actor._
import akka.pattern.pipe
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax._
import com.github.osxhacker.foundation.models.core.error._
import com.github.osxhacker.foundation.models.core.net
import com.github.osxhacker.foundation.models.core.time
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.password._


/**
 * The '''PasswordAdministrator''' type defines the Domain Object Model
 * [[akka.actor.Actor]] responsible for managing
 * [[com.github.osxhacker.foundation.models.security.password.SystemPassword]]s.
 *
 * @author osxhacker
 *
 */
final class PasswordAdministrator ()
	extends FSM[PasswordAdministrator.State, PasswordAdministrator.Context]
		with ActorEventSubscriber[PasswordAdministratorEvent]
		with LocalMaterializer
		with MonitorLike
		with ReplyTo
		with SettingsAware[SecuritySettings]
		with Stash
		with Slf4jLogging
		with PasswordAdministratorOperations
{
	/// Class Imports
	import PasswordAdministrator._
	import context.dispatcher
	import net.URI
	import syntax.either._
	import syntax.std.option._


	/// Instance Properties
	implicit override val settings = SecuritySettings (context.system);

	override val retries = settings.retries;
	override val timeRange = settings.timeRange;


	/// Constructor Body
	startWith (Initializing, Uninitialized);


	/// The Initializing state is the first entered and initiates resolution of
	/// '''SystemPasswordStorageLocator'''.  Once resolved, the
	/// '''PasswordAdministrator''' becomes Active and ready for use.
	when (Initializing, stateTimeout = settings.loadingTimeout) {
		case Event (StateTimeout, _) =>
			error (
				"initializing password administrator timed out actor={}",
				self.path
				);

			goto (Errored) using (
				DetectedError (InitializationError ("timeout"))
				);

		case Event (\/- (storage : SystemPasswordStorage), _) =>
			debug (
				"password administrator resolved storage actor={}",
				self.path
				);

			settings.deployment.fold (
				e => goto (Errored) using (DetectedError (e)),
				env => {

					discover (storage).run
						.pipeTo (self);

					goto (Loading) using (CachedEntries (storage));
					}
				);

		case Event (-\/ (failure : ApplicationError), _) =>
			error (
				"password administrator unable to resolve storage actor={} error={}",
				self.path,
				failure
				);

			goto (Errored) using (DetectedError (failure));

		case Event (-\/ (failure : Throwable), _) =>
			error (
				"unknown error in password administrator actor={} error={}",
				self.path,
				failure
				);

			goto (Errored) using (
				DetectedError (InitializationError (failure.getMessage))
				);
		}


	/// The Active state is entered into when the '''PasswordAdministrator'''
	/// can satisfy requests from clients.
	when (Active) {
		case Event (
			FindPasswordRequest (logical),
			cache @ CachedEntries (_, existing, _)
			) =>

			existing.get (logical) cata (
				foundPassword,
				tryLoading (logical, cache)
				);

		case Event (_ : FlushPasswordsEvent, CachedEntries (storage, _, _)) =>
			debug (
				"password administrator flushing passwords actor={}",
				self.path
				);

			stay () using (CachedEntries (storage));

		case Event (_ : ListPasswordsRequest, cache : CachedEntries) =>
			replyTo (sender ()) {
				ListPasswordsResponse (
					cache.toVector.right[ApplicationError]
					);
				}

			stay ();
		}


	/// The Loading state is entered into whenever the
	/// '''PasswordAdministrator''' is resolving '''SystemPassword'''s __or__
	/// when a known '''SystemPassword''' has surpassed its time-to-live.
	when (Loading, stateTimeout = settings.loadingTimeout) {
		case Event (StateTimeout, InFlight (requestor, _, target, cache)) =>
			error (
				"password administrator failed to load actor={} requestor={} target={}",
				self.path,
				requestor.path,
				target
				);

			goto (Active) using (cache);

		case Event (
			\/- (clear : CleartextPassword),
			InFlight (requestor, logical, target, cache)
			) =>
			debug (
				"resolved password actor={} requestor={} logical={} target={}",
				self.path,
				requestor,
				logical,
				target
				);

			val password = SystemPassword (logical, clear);

			replyTo (requestor) {
				FindPasswordResponse (password.right[ApplicationError]);
				}

			goto (Active) using (cache.add (password, target, clear));

		case Event (
			\/- (DiscoveredPasswords (known)),
			CachedEntries (storage, _, _)
			) =>
			debug ("password administrator finished discovery");

			goto (Active) using (CachedEntries (storage, known));

		case Event (
			-\/ (failure : ApplicationError),
			InFlight (requestor, _, target, cache)
			) =>
			error (
				"password administrator unable to find p/w actor={} target={} error={}",
				self.path,
				target,
				failure
				);

			replyTo (requestor) {
				FindPasswordResponse (failure.left[SystemPassword]);
				}

			goto (Active) using (cache);

		case Event (-\/ (failure : ApplicationError), _) =>
			error (
				"password administrator failed while loading actor={} error={}",
				self.path,
				failure
				);

			goto (Errored) using (DetectedError (failure));
		}


	/// The Errored state is entered into for a finite time as a result of
	/// an inability to resolve '''SystemPasswordStorage''' or in response to an
	/// unknown run-time failure.  After the configured errorTimeout, an
	/// attempt to initialize is started.
	when (Errored, stateTimeout = settings.errorTimeout) {
		case Event (StateTimeout, _) =>
			debug (
				"errored password administrator recovering actor={}",
				self.path
				);

			goto (Initializing) using (Uninitialized);

		/// In the case where the loading result is received after Initializing
		/// timed out, interpret the result here as well.
		case Event (\/- (storage : SystemPasswordStorage), _) =>
			debug (
				"password administrator (Errored) resolved storage actor={}",
				self.path
				);

			goto (Active) using (CachedEntries (storage));

		case Event (-\/ (failure : ApplicationError), _) =>
			error (
				"password administrator (Errored) unable to resolve storage actor={} error={}",
				self.path,
				failure
				);

			stay () using (DetectedError (failure));

		case Event (msg, DetectedError (failure)) =>
			info (
				"password administrator received request while errored actor={} sender={} message={}",
				self.path,
				sender ().path,
				msg
				);

			sender () ! Status.Failure (failure);
			stay ();
		}


	onTransition {
		case _ -> Initializing =>
			resolveStorage (settings) (context.system).run
				.pipeTo (self);

		case _ -> Active =>
			unstashAll ();

		case _ -> Errored =>
			unstashAll ();
		}


	whenUnhandled {
		case Event (msg, _) =>
			debug (
				"delaying password supervisor message actor={} state={} message={}",
				self.path,
				stateName,
				msg
				);

			stash ();
			stay ();
		}


	initialize ();


	override def postStop () : Unit =
	{
		unsubscribe ();
		super.postStop ();
	}


	override def preStart () : Unit =
	{
		super.preStart ();
		subscribe ();
	}


	private def foundPassword (password : SystemPassword) : State =
	{
		replyTo (sender ()) {
			FindPasswordResponse (password.right[ApplicationError]);
			}
		
		return stay ();
	}


	private def tryLoading (logical : URI, cache : CachedEntries) : State =
		locationFor (StorageIndicator (settings.passwords), logical) fold (
			failure => {
				replyTo (sender ()) {
					FindPasswordResponse (failure.left[SystemPassword]);
					}

				goto (Active) using (cache);
				},

			physical => {
				find (physical, cache.storage).run
					.pipeTo (self);

				goto (Loading) using (
					InFlight (sender (), logical, physical, cache)
					);
				}
			);
}


object PasswordAdministrator
{
	/// Class Imports
	import net.URI


	/// Class Types
	sealed trait State


	case object Active
		extends State


	case object Errored
		extends State


	case object Initializing
		extends State


	case object Loading
		extends State


	sealed trait Context


	case class CachedEntries (
		val storage : SystemPasswordStorage,
		val known : Map[URI, SystemPassword],
		val passwords : Map[URI @@ StorageIndicator, CleartextPassword]
		)
		extends Context
			with Iterable[URI]
	{
		/// Class Imports
		import std.tuple._
		import syntax.functor._


		override def iterator = passwords.keys
			.map (Tag.unwrap)
			.iterator;


		def add (
			system : SystemPassword,
			location : URI @@ StorageIndicator,
			clear : CleartextPassword
			) =
			copy (
				known = known.updated (system.logical, system),
				passwords = passwords.updated (location, clear)
				);
	}


	object CachedEntries
	{
		def apply (storage : SystemPasswordStorage) : CachedEntries =
			new CachedEntries (storage, Map.empty, Map.empty);

		def apply (
			storage : SystemPasswordStorage,
			passwords : Map[URI @@ StorageIndicator, CleartextPassword]
			) : CachedEntries =
			new CachedEntries (storage, Map.empty, passwords);
	}


	case class DetectedError (failure : ApplicationError)
		extends Context


	case class InFlight (
		val requestor : ActorRef,
		val logical : URI,
		val target : URI @@ StorageIndicator,
		val cache : CachedEntries
		)
		extends Context


	case object Uninitialized
		extends Context


	/**
	 * The apply method is provided so that the details of how to instantiate a
	 * '''PasswordAdministrator''' instance is insulated from clients.
	 */
	def apply ()
		(implicit factory : ActorRefFactory)
		: ActorRef =
		factory.actorOf (
			Props[PasswordAdministrator] (new PasswordAdministrator ())
			);
}

