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

package com.github.osxhacker.foundation.models.security.vault

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import org.scalatest.DiagrammedAssertions
import scalaz.{
	:+: => _,
	Coproduct => _,
	Failure => _,
	Id => _,
	Success => _,
	_
	}

import shapeless.{
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	BehaviorSpecLike,
	Identifier
	}

import com.github.osxhacker.foundation.models.core.akkax.ActorBasedSpec
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.security.encryption.AES


/**
 * The '''ObjectVaultingSpec''' type defines a '''BehaviorSpec''' for defining
 * the requirements which a
 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] must satisfy when
 * operating with arbitray ADT's having zero or more
 * [[com.github.osxhacker.foundation.models.security.vault.Vaulted]] properties.
 *
 * @author osxhacker
 *
 */
final class ObjectVaultingSpec
	extends ActorBasedSpec ("test-object-vaulting")
		with DiagrammedAssertions
		with BehaviorSpecLike
		with VaultSupport
{
	/// Class Imports
	import functional._
	import functional.futures._
	import functional.futures.comonad._
	import std.list._
	import syntax.all._


	/// Instance Properties
	implicit val keyStorage = MockKeyStorage[AES] ();
	lazy val safe = VaultFactory ("test").apply ();


	info ("A key benefit provided by the Vault is being able to store and");
	info ("retrieve properties within arbitrary ADT's automatically");


	feature ("Supporting simple ADT's") {
		val text = "super secret";
		val original = SensitiveInfo (Decrypted (text));

		scenario ("Storing one in a Vault") {
			Given ("a decrypted SensitiveInfo");

			assert (original.what.isInstanceOf[Decrypted[String]]);

			When ("stored in the vault");

			val vaulted = safe >>= (_.store (original));

			Then ("the operation should succeed");

			assert (vaulted.isRight, vaulted.toString);

			And ("the vaulted property is now encrypted");

			assert (
				vaulted.exists (_.what.isInstanceOf[Encrypted[String]]),
				vaulted.map (_.what.getClass.getName)
				);
			}

		scenario ("Storing multiple in a Vault") {
			Given ("multiple decrypted SensitiveInfo");

			val multiple = List.fill (20) (original);

			assert (multiple.forall (_.what.isInstanceOf[Decrypted[String]]));

			When ("storing multiple in the vault");

			val vaulted = safe >>= {
				theVault =>

				multiple.traverseU (a => theVault.store (a));
				}

			Then ("all of the operations should succeed");

			assert (vaulted.isRight, vaulted.toString);

			And ("all of the vaulted properties are now encrypted");

			assert (
				vaulted.exists (_.forall (_.what.isInstanceOf[Encrypted[String]]))
				);
			}

		scenario ("Retrieving one from a Vault") {
			Given ("a decrypted SensitiveInfo");

			assert (original.what.isInstanceOf[Decrypted[String]]);

			And ("stored in the vault");

			val vaulted = safe >>= (_.store (original));

			assert (vaulted.isRight, vaulted.toString);
			assert (
				vaulted.exists (_.what.isInstanceOf[Encrypted[String]]),
				vaulted.map (_.what.getClass.getName)
				);

			When ("retrieved from the vault");

			val unvaulted = for {
				theVault <- safe
				instance <- vaulted
				retrieved <- theVault.retrieve (instance)
				} yield retrieved;

			Then ("retrieval should have succeeded");

			assert (unvaulted.isRight, unvaulted.toString);

			And ("the vaulted property is now decrypted");

			assert (
				unvaulted.exists (_.what.isInstanceOf[Decrypted[String]]),
				unvaulted.map (_.what.getClass.getName)
				);

			assert (
				unvaulted.map (si => new String (si.what.content)) ==
					\/- (text)
				);
			}
		}

	feature ("Supporting nested ADT's") {
		val text = "super secret";
		val age = "42";
		val original = SamplePerson (
			name = SensitiveInfo (Decrypted (text)),
			age = Some (Decrypted (age))
			);

		scenario ("Storing one in a Vault") {
			Given ("a SamplePerson");

			assert (original.age.exists (_.isInstanceOf[Decrypted[String]]));
			assert (original.name.what.isInstanceOf[Decrypted[String]]);

			When ("placed in the vault");

			val vaulted = safe >>= (_.store (original));

			Then ("the operation should have succeeded");

			assert (vaulted.isRight, vaulted.toString);

			And ("the top-level vaulted property is now encrypted");

			assert (vaulted.exists (_.age.isDefined));
			assert (
				vaulted.exists (
					_.age.exists (_.isInstanceOf[Encrypted[String]])
					),

				vaulted
				);

			And ("the nested vaulted property is now encrypted");

			assert (
				vaulted.exists (_.name.what.isInstanceOf[Encrypted[String]]),
				vaulted.map (_.name.what.getClass.getName)
				);
			}

		scenario ("Storing one within an Option in a Vault") {
			Given ("a SamplePerson in an Option");

			val wrapped = Option (original);

			assert (
				wrapped.exists (
					_.age.exists (_.isInstanceOf[Decrypted[String]])
					)
				);

			assert (
				wrapped.exists (_.name.what.isInstanceOf[Decrypted[String]])
				);

			When ("placed in the vault");

			val vaulted = safe >>= (_.store (wrapped));

			Then ("the operation should have succeeded");

			assert (vaulted.isRight, vaulted.toString);

			And ("the top-level vaulted property is now encrypted");

			assert (vaulted.exists (_.exists (_.age.isDefined)));
			assert (
				vaulted.exists (
					_.exists (
						_.age.exists (_.isInstanceOf[Encrypted[String]])
						)
					),

				vaulted
				);

			And ("the nested vaulted property is now encrypted");

			assert (
				vaulted.exists (
					_.exists (_.name.what.isInstanceOf[Encrypted[String]])
					),

				vaulted.map (_.map (_.name.what.getClass.getName))
				);
			}
		}
}

