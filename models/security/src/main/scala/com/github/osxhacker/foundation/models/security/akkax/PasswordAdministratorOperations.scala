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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Deadline
import scala.language.postfixOps

import akka.actor.{
	ActorRef,
	ActorSystem
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.DeploymentEnvironment
import com.github.osxhacker.foundation.models.core.akkax.{
	MaterializerAware,
	streams
	}

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError
	}

import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.net
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.akkax.extension.SystemPasswordStorageLocator
import com.github.osxhacker.foundation.models.security.password._


/**
 * The '''PasswordAdministratorOperations''' trait defines the domain
 * behaviour available to
 * [[com.github.osxhacker.foundation.models.security.akkax.PasswordAdministrator]] which
 * is __independent__ of internal [[akka.actor.Actor]] implementation concerns.
 *
 * @author osxhacker
 *
 */
trait PasswordAdministratorOperations
{
	/// Self Type Constraints
	this : MaterializerAware =>


	/// Class Imports
	import functional.futures._
	import functional.futures.monad._
	import net.URI
	import net.uri._
	import std.vector._
	import streams._
	import syntax.either._
	import syntax.std.option._


	/**
	 * The discover method attempts to resolve, or "discover", all
	 * [[com.github.osxhacker.foundation.models.security.password.SystemPassword]]s known
	 * __at the time of invocation__.  This does __not__ obviate the need to
	 * do run-time `load`ing in attempt to resolve instances later, as the
	 * environment could have entries added after the system starts.
	 */
	def discover (storage : SystemPasswordStorage)
		(implicit ec : ExecutionContext, settings : SecuritySettings)
		: FutureEither[DiscoveredPasswords] =
		storage.query (StorageIndicator (settings.passwords))
			.toFutureEitherCollection[Vector]
			.map {
				entries =>

				DiscoveredPasswords (entries.toMap);
				}


	/**
	 * The find method requests the given '''storage''' to resolve a
	 * [[com.github.osxhacker.foundation.models.security.password.CleartextPassword]] by
	 * using its '''physical''' [[com.github.osxhacker.foundation.models.core.net.URI]].
	 */
	def find (
		physical : URI @@ StorageIndicator,
		storage : SystemPasswordStorage
		)
		: FutureEither[CleartextPassword] =
		storage.find (physical)
			.toFutureEither;


	/**
	 * The locationFor method attempts to combine the '''root'''
	 * [[com.github.osxhacker.foundation.models.core.net.URI]] with the '''logical'''
	 * [[com.github.osxhacker.foundation.models.core.net.URI]] and its "user-info" in
	 * order to produce a normalized expected "location."
	 */
	def locationFor (root : URI @@ StorageIndicator, logical : URI)
		: ApplicationError \/ (URI @@ StorageIndicator) =
	{
		val account = Option (logical.getUserInfo) \/> DomainValueError (
			s"missing user-info for system password uri=${logical}"
			);

		return account map {
			name =>

			StorageIndicator (Tag.unwrap (root) / logical.getPath () / name);
			}
	}


	/**
	 * The resolveStorage method attempts to produce a
	 * [[com.github.osxhacker.foundation.models.security.password.SystemPasswordStorage]]
	 * made available via the
	 * [[com.github.osxhacker.foundation.models.security.akkax.extension.SystemPasswordStorageLocator]].
	 */
	def resolveStorage (settings : SecuritySettings)
		(implicit system : ActorSystem)
		: FutureEither[SystemPasswordStorage] =
	{
		implicit val deadline = settings.loadingTimeout fromNow;

		return SystemPasswordStorageLocator (system) ();
	}
}

