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
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax._
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	InitializationError,
	LogicError
	}

import com.github.osxhacker.foundation.models.core.time
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.error.UnknownKeyVersionError
import com.github.osxhacker.foundation.models.security.encryption._
import com.github.osxhacker.foundation.models.security.vault._


/**
 * The '''VaultAdministrator''' type defines the Domain Object Model
 * [[akka.actor.Actor]] responsible for coordinating access to one or more
 * [[com.github.osxhacker.foundation.models.security.vault.Vault]]s.  To support
 * key migration, requests having a specific
 * [[com.github.osxhacker.foundation.models.security.encryption.KeyVersion]] use the
 * one requested, whereas requests lacking same will use the "newest"
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]].
 *
 * @author osxhacker
 *
 */
final class VaultAdministrator ()
	(implicit private val storage : KeyStorage)
	extends FSM[VaultAdministrator.State, VaultAdministrator.Context]
		with LocalMaterializer
		with MonitorLike
		with ReplyTo
		with Stash
		with Slf4jLogging
		with VaultAdministratorOperations[AES]
{
	/// Class Imports
	import VaultAdministrator._
	import syntax.either._
	import syntax.std.option._


	/// Instance Properties
	private val settings = SecuritySettings (context.system);
	
	override val retries = settings.retries;
	override val timeRange = settings.timeRange;
	
	private val duplicates = settings.vaultCopies | 5;


	/// Constructor Body
	startWith (Initializing, Uninitialized);


	/// The Initializing state is the first entered and initiates resolution of
	/// all ActiveVault.  Once resolved, the '''VaultAdministrator''' becomes
	/// Active and ready for use.
	when (Initializing, stateTimeout = settings.loadingTimeout) {
		case Event (StateTimeout, _) =>
			error (
				"initializing vault administrator timed out actor={}",
				self.path
				);

			goto (Errored) using (
				DetectedError (InitializationError ("timeout"))
				);

		case Event (LoadingKeysResult (result), _) =>
			debug (
				"initializing vault administrator complete actor={} result={}",
				self.path,
				result
				);

			result fold (
				e => goto (Errored) using (DetectedError (e)),
				keys => goto (Active) using (
					new ActiveVault (
						List.fill (duplicates) (createVaultFromKeys (keys))
						)
					)
				);
		}


	/// The Active state is entered into when the '''VaultAdministrator''' can
	/// satisfy requests from clients using the '''ActiveVault''' at this
	/// point in time.
	when (Active) {
		case Event (MostRecentVaultRequest (), known : ActiveVault) =>
			val (vault, active) = known.cycle ();

			replyTo (sender ()) {
				MostRecentVaultResponse (vault.right);
				}

			stay () using (active);

		case Event (unknown : VaultAdministratorRequest[_], _) =>
			error (
				"unexpected vault admin message received actor={} message={}",
				self.path,
				unknown
				);
			
			self forward unknown;
			goto (Errored) using (DetectedError (LogicError ("active state")));
		}


	/// The Errored state is entered into for a finite time as a result of
	/// an inability to resolve '''ActiveVault''' or in response to an
	/// unknown run-time failure.  After the configured errorTimeout, an
	/// attempt to initialize is started.
	when (Errored, stateTimeout = settings.errorTimeout) {
		case Event (StateTimeout, _) =>
			debug (
				"errored vault administrator recovering actor={}",
				self.path
				);

			goto (Initializing) using (Uninitialized);

		/// In the case where the loading result is received after Initializing
		/// timed out, interpret the result here as well.
		case Event (LoadingKeysResult (result), _) =>
			debug (
				"vault administrator loading keys (Errored) actor={} result={}",
				self.path,
				result
				);

			result fold (
				e => stay () using (DetectedError (e)),
				keys => goto (Active) using (
					new ActiveVault (
						List.fill (duplicates) (createVaultFromKeys (keys))
						)
					)
				);

		case Event (msg, DetectedError (failure)) =>
			info (
				"vault administrator received request while errored actor={} sender={} message={}",
				self.path,
				sender ().path,
				msg
				);

			sender () ! Status.Failure (failure);
			stay ();
		}


	onTransition {
		case _ -> Initializing =>
			loadKnownKeys (self, settings);

		case _ -> Active =>
			unstashAll ();

		case _ -> Errored =>
			unstashAll ();
		}


	whenUnhandled {
		case Event (msg, _) =>
			debug (
				"delaying vault supervisor message vault={} message={}",
				self.path,
				msg
				);

			stash ();
			stay ();
		}


	initialize ();
}


object VaultAdministrator
{
	/// Class Imports
	import time.statics._
	import time.types._


	/// Class Types
	sealed trait State


	case object Active
		extends State


	case object Errored
		extends State


	case object Initializing
		extends State


	sealed trait Context


	case class DetectedError (failure : ApplicationError)
		extends Context


	case class ActiveVault (private val queue : Stream[Vault[AES]])
		extends Context
	{
		def this (vaults : List[Vault[AES]]) =
			this (Stream.continually (vaults).flatten);


		def cycle () : (Vault[AES], ActiveVault) =
			(queue.head, copy (queue.tail));
	}


	case object Uninitialized
		extends Context


	/// Instance Properties
	private val actorName = "vault-supervisor";


	/**
	 * The apply method is provided so that the details of how to instantiate a
	 * '''VaultAdministrator''' instance is insulated from clients.
	 */
	def apply (storage : KeyStorage)
		(implicit system : ActorSystem)
		: ActorRef =
		system.actorOf (
			Props[VaultAdministrator] (
				new VaultAdministrator () (storage)
				),

			actorName
			);


	/**
	 * The path method provides the caller with an [[akka.actor.ActorPath]]
	 * suitable for resolving the '''VaultAdministrator''' assumed to be
	 * running within a local [[akka.actor.ActorSystem]].
	 */
	def path (system : ActorSystem) : ActorPath = system / actorName;
}
