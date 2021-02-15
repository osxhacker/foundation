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
package extension

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

import akka.NotUsed
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	Zip => _,
	_
	}

import shapeless.{
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.akkax.{
	ActorSystemAware,
	MessagingClient
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.encryption.AES
import com.github.osxhacker.foundation.models.security.vault._


/**
 * The '''ObjectVault''' type defines an [[akka.actor.Extension]] which can
 * `vault` and `unvault` arbitrary ADT's.  Since [[shapeless]] is leveraged,
 * generic logic is performed here.
 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] management is
 * delegated to the
 * [[com.github.osxhacker.foundation.models.security.akkax.VaultAdministrator]]
 * [[akka.actor.Actor]].
 *
 * @author osxhacker
 *
 */
final class ObjectVault (private val administrator : ActorRef)
	(implicit override val system : ActorSystem)
	extends Extension
		with ActorSystemAware
{
	/// Class Imports
	import akkax.streams._
	import functional._
	import functional.futures._
	import functional.futures.monad._
	import syntax.all._


	/// Instance Properties
	implicit private val ec = system.dispatcher;

	private val client = MessagingClient[
		VaultAdministratorResponse,
		VaultAdministratorRequest
		] (administrator);


	/**
	 * The apply method delegates the specific
	 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] operation to
	 * invoke so that the call site can provide the `implicit`s each
	 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] method requires.
	 * Since using the most recent
	 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] available is a
	 * common usage scenario, it is made easiest to use.
	 */
	def apply[A] (f : Vault[AES] => ApplicationError \/ A)
		(implicit deadline : Deadline)
		: FutureEither[A] =
		(client ? MostRecentVaultRequest ()).flatMapE {
			response =>

			for {
				vault <- response.result
				transformed <- f (vault)
				} yield transformed;
			}


	/**
	 * The flow method produces a [[akka.stream.scaladsl.Flow]] which will
	 * resolve what [[com.github.osxhacker.foundation.models.security.vault.Vault]] to
	 * use in downstream operations.
	 */
	def flow[A] (f : (A, Vault[AES]) => ApplicationError \/ A)
		(implicit duration : FiniteDuration)
		: Graph[FlowShape[A, ApplicationError \/ A], NotUsed] =
		GraphDSL.create () {
			implicit builder =>

			import GraphDSL.Implicits._

			val broadcast = builder.add (Broadcast[A] (2));

			val resolveVault = builder.add (
				Flow[A].requestOf (administrator) (_ => MostRecentVaultRequest ())
					.map (_.result)
					.valueOrFail ()
				);

			val transformer = builder.add (Flow.fromFunction (f.tupled));

			val zip = builder.add (Zip[A, Vault[AES]] ());

			broadcast.out (0)							~>			zip.in0
			broadcast.out (1)		~>	resolveVault	~>			zip.in1

			zip.out					~>	transformer.in

			FlowShape (broadcast.in, transformer.out);
			}
}


object ObjectVault
	extends ExtensionId[ObjectVault]
		with ExtensionIdProvider
{
	/// Class Imports
	import functional.futures.comonad._
	import syntax.comonad._


	override def createExtension (system : ExtendedActorSystem) : ObjectVault =
	{
		val settings = SecuritySettings (system);

		implicit val ec = system.dispatcher;
		implicit val deadline = settings.loadingTimeout fromNow;

		val admin = system.actorSelection (VaultAdministrator.path (system))
			.resolveOne (settings.loadingTimeout)
			.copoint;

		return new ObjectVault (admin) (system);
	}


	override def lookup () = this;
}

