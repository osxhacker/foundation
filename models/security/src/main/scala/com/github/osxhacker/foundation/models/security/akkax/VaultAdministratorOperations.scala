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

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.{
	ActorRef,
	ActorRefFactory
	}

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.MaterializerAware
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	ConfigurationError,
	InvalidModelStateError
	}

import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.error.UnknownKeyVersionError
import com.github.osxhacker.foundation.models.security.encryption._
import com.github.osxhacker.foundation.models.security.vault._


/**
 * The '''VaultAdministratorOperations''' trait defines the domain behaviour
 * available to the
 * [[com.github.osxhacker.foundation.models.security.akkax.VaultAdministrator]].
 *
 * @author osxhacker
 *
 */
trait VaultAdministratorOperations[AlgoT <: Algorithm[AlgoT]]
{
	/// Self Type Constraints
	this : MaterializerAware =>


	/// Class Imports
	import syntax.all._
	import syntax.std.all._


	/**
	 * The createVaultFromKeys method produces a
	 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] having all of the
	 * '''keys''' available to it for vaulting operations.
	 */
	def createVaultFromKeys (keys : Seq[CipherKeys])
		(implicit a : AlgoT)
		: Vault[AlgoT] =
		Vault[AlgoT] (keys);


	/**
	 * The findVaultFor method will either resolve the
	 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] uniquely identified
	 * by its [[com.github.osxhacker.foundation.models.security.encryption.KeyVersion]] or
	 * fail the retrieval request.
	 */
	def findVaultFor (
		version : KeyVersion,
		vaults : Map[KeyVersion, Vault[AlgoT]]
		)
		: ApplicationError \/ Vault[AlgoT] =
		vaults.get (version) \/> UnknownKeyVersionError (version);


	/**
	 * The loadKnownKeys method initiates the resolution of the
	 * by its [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]]
	 * provided by '''storage''' __at the time of invocation__.
	 */
	def loadKnownKeys (administrator : ActorRef, settings : SecuritySettings)
		(implicit factory : ActorRefFactory, storage : KeyStorage)
		: Unit =
		settings.deployment match {
			case \/- (env) =>
				val millis = (settings.loadingTimeout.toMillis * 0.80).toLong;

				RefreshCipherKeys (
					administrator,
					env,
					FiniteDuration (millis, MILLISECONDS) fromNow
					);

			case -\/ (err) =>
				administrator ! LoadingKeysResult (
					ConfigurationError ("invalid deployment environment", err)
					);
			}
}
