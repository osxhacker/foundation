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

package com.github.osxhacker.foundation.models.security.akkax.extension

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.stream.ActorMaterializer
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.BehaviorSpecLike
import com.github.osxhacker.foundation.models.core.akkax.ActorBasedSpec
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.akkax.VaultAdministrator
import com.github.osxhacker.foundation.models.security.encryption._
import com.github.osxhacker.foundation.models.security.vault._


/**
 * The '''VaultObjectWorkflowSpec''' type defines the behavioural requirements
 * which define the idiomatic workflow involved in "vaulting" an arbitrary
 * ADT using the
 * [[com.github.osxhacker.foundation.models.security.akkax.extension.ObjectVault]] Akka
 * extension.
 *
 * @author osxhacker
 *
 */
final class VaultObjectWorkflowSpec
	extends ActorBasedSpec ("test-vault-object-workflow")
		with BehaviorSpecLike
		with VaultSupport
{
	/// Class Imports
	import functional._
	import functional.futures.comonad._
	import syntax.all._
	import system.dispatcher


	/// Instance Properties
	implicit lazy val materializer = ActorMaterializer ();

	lazy val keyStorage = MockKeyStorage[AES] ();
	lazy val settings = SecuritySettings (system);


	override def beforeAll () : Unit =
	{
		super.beforeAll ();

		/// In a production deployment, an OSGi activator is responsible for
		/// starting the VaultAdministrator.
		VaultAdministrator (keyStorage);
	}


	feature ("Vaulting a domain object") {
		info ("Whenever a domain object needs to ensure its sensitive");
		info ("information is secured, the ObjectVault.store method should");
		info ("be used.");

		scenario ("Providing one with top-level Vaulted properties") {
			implicit val deadline = 5 seconds fromNow;

			Given ("an unencrypted instance");

			val unencrypted = SensitiveInfo (Decrypted ("some text"));

			When ("the extension applies the 'store' operation");

			val future = ObjectVault (system) {
				_.store (unencrypted);
				}

			val result = future.run.copoint;

			Then ("the operation should succeed");

			assert (result.isRight, result.toString);

			And ("the top-level property should be encrypted");

			result foreach {
				info =>

				assert (info.what.isInstanceOf[Encrypted[String]]);
				}

			And ("there should be one key in the key storage");
			assert (keyStorage.keys.length === 1);
			}

		scenario ("Providing one with nested Vaulted properties") {
			implicit val deadline = 5 seconds fromNow;

			Given ("an unencrypted instance");

			val unencrypted = SamplePerson (
				SensitiveInfo (Decrypted ("some text")),
				age = None
				);

			When ("the extension applies the 'store' operation");

			val future = ObjectVault (system) {
				_.store (unencrypted);
				}

			val result = future.run.copoint;

			Then ("the operation should succeed");

			assert (result.isRight, result.toString);

			And ("the top-level property should be encrypted");

			result foreach {
				person =>

				assert (person.name.what.isInstanceOf[Encrypted[String]]);
				}

			And ("there should be one key in the key storage");
			assert (keyStorage.keys.length === 1);
			}
		}

	feature ("Unvaulting a domain object") {
		info ("Whenever a domain object needs to access its sensitive");
		info ("information, the ObjectVault.retrieve method should be used.");

		scenario ("Retrieving one with top-level Vaulted properties") {
			implicit val deadline = 5 seconds fromNow;

			Given ("an unencrypted instance");

			val unencrypted = SensitiveInfo (Decrypted ("some text"));

			And ("storing it in the vault");

			val stored = ObjectVault (system) {
				_.store (unencrypted);
				}

			When ("the extension applies the 'retrieve' operation");

			val future = stored >>= {
				instance =>

				ObjectVault (system) {
					_.retrieve (instance);
					}
				}

			val result = future.run.copoint;

			Then ("the operation should succeed");

			assert (result.isRight, result.toString);

			And ("the top-level property should be decrypted");

			result foreach {
				info =>

				assert (info.what.isInstanceOf[Decrypted[String]]);
				assert (
					info.what.extract[ErrorOr] (bytes => new String (bytes)) ==
					\/- ("some text")
					);
				}

			And ("there should be one key in the key storage");
			assert (keyStorage.keys.length === 1);
			}
		}
}
