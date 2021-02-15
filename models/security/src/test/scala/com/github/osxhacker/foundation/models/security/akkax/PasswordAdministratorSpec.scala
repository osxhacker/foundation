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

import akka.stream.ActorMaterializer
import org.scalatest.DiagrammedAssertions
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
import com.github.osxhacker.foundation.models.core.net
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.akkax._
import com.github.osxhacker.foundation.models.security.akkax.extension.{
	SystemPasswordEntries,
	SystemPasswordStorageLocator
	}

import com.github.osxhacker.foundation.models.security.password._


/**
 * The '''PasswordAdministratorSpec''' type defines the unit-tests which
 * certify the
 * [[com.github.osxhacker.foundation.models.security.akkax.PasswordAdministrator]] for
 * fitness of purpose and serves as an exemplar of its use.
 *
 * @author osxhacker
 *
 */
final class PasswordAdministratorSpec ()
	extends ActorBasedSpec ("test-password-administrator")
		with BehaviorSpecLike
		with PasswordSupport
		with DiagrammedAssertions
{
	/// Class Imports
	import functional.futures.comonad._
	import net.URI
	import net.uri._
	import syntax.comonad._
	import system.dispatcher


	/// Instance Properties
	val physical = new URI ("vault:/unit-test/passwords");
	lazy val storage = MockSystemPasswordStorage (
		(physical / "subsystem" / "bob") -> CleartextPassword (
			"the password"
			) ::
		(physical / "subsystem" / "alice") -> CleartextPassword (
			"alice's p/w"
			) ::
		Nil
		);


	override def beforeAll () : Unit =
	{
		super.beforeAll ();

		/// In a production deployment, an OSGi activator is responsible for
		/// providing the SystemPasswordStorage
		SystemPasswordStorageLocator.register (storage);
	}


	feature ("Resolving system passwords") {
		info ("The system must be able to define and use passwords which are");
		info ("known to it in order to interact with external actors.");

		scenario ("Finding a known system password") {
			implicit val deadline = 5 seconds fromNow;

			Given ("a 'logical' URI known to the storage");

			val bob = new URI ("system-password://bob@localhost/subsystem");

			When ("requesting the system password for it");

			val future = SystemPasswordEntries (system) ? FindPasswordRequest (
				bob
				);

			val result = future.run
				.copoint;

			Then ("the request should succeed");

			assert (result.isRight);

			And ("it should contain a SystemPassword instance");

			result.map (_.result).foreach {
				case \/- (password) =>
					assert (password.account.isRight);
					assert (password.host.isRight);
					assert (password.port.isEmpty);
					assert (password.logical === bob);

				case -\/ (error) =>
					fail (error.getMessage);
				}
			}

		scenario ("Finding an unknown system password") {
			implicit val deadline = 5 seconds fromNow;

			Given ("a 'logical' URI not known to the storage");

			val unknown = new URI ("system-password://a@localhost/subsystem");

			When ("requesting the system password for it");

			val future = SystemPasswordEntries (system) ? FindPasswordRequest (
				unknown
				);

			val result = future.run
				.copoint;

			Then ("the request should succeed");

			assert (result.isRight);

			And ("it should contain a SystemPassword instance");

			result.map (_.result).foreach {
				case \/- (password) =>
					fail ("resolved a password when it should not have");

				case -\/ (error) =>
					assert (error ne null);
				}
			}

		scenario ("Listing all known system passwords") {
			implicit val deadline = 5 seconds fromNow;

			Given ("an initialized storage system");

			assert (storage.passwords.size > 0);

			When ("requesting to list ");

			val future = SystemPasswordEntries (system) ? (
				ListPasswordsRequest ()
				);

			val result = future.run
				.copoint;

			Then ("the request should succeed");

			assert (result.isRight);

			And ("it should contain all known SystemPasswords");

			result.map (_.result).foreach {
				case \/- (passwords) =>
					assert (passwords.size === storage.passwords.size);

				case -\/ (error) =>
					fail (error.getMessage);
				}
			}
		}
}
