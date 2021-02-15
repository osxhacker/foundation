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

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	BehaviorSpecLike,
	Identifier
	}

import com.github.osxhacker.foundation.models.core.akkax.ActorBasedSpec
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.text.encoding.Base64
import com.github.osxhacker.foundation.models.security.encryption.{
	AES,
	KeyStorage
	}

import com.github.osxhacker.foundation.models.security.scenario.CreateVault


/**
 * The '''BuiltinsSpec''' type defines a '''BehaviorSpec''' which defines the
 * requirements for securing Scala built-ins, such as `String` and
 * `Array[Byte]`.
 *
 * @author osxhacker
 *
 */
final class BuiltinsSpec
	extends ActorBasedSpec ("test-builtins")
		with BehaviorSpecLike
		with VaultSupport
{
	/// Class Imports
	import functional._
	import functional.futures._
	import functional.futures.comonad._
	import syntax.all._


	/// Instance Properties
	implicit val ec = ExecutionContext.global;
	implicit val expiry = 5 seconds fromNow;
	implicit val keyStorage = MockKeyStorage[AES] ();
	lazy val safe = VaultFactory ("test").apply ();


	info ("The simplest use of a vault is when securing built-ins");
	
	
	feature ("Vaulting built-in types") {
		scenario ("Putting 'hello world' into the vault") {
			Given ("the plain-text representation of 'hello world'");

			val original = Decrypted ("hello world");

			When ("it is put into the vault");
			
			val result = safe >>= (_.store (original));

			Then ("what is in the vault is encrypted");
			
			assert (result.isRight, result.toString);
			assert (result.exists (_.content ne null));
			assert (
				result.map (_.toEncoded[Base64])
					.exists (_.startsWith ("{"))
				);

			assert (
				result.map (_.toEncoded[Base64])
					.exists (_.contains ("}"))
				);
			}
		
		scenario ("Getting 'hello world' out of the vault") {
			Given ("the plain-text representation of 'hello world'");

			val original = "hello world";

			And ("putting it into the vault");
			
			val stored = safe >>= (_.store (Decrypted (original)));

			When ("retrieving it from the vault");
			
			val result = for {
				vault <- safe
				shrouded <- stored
				unshrouded <- vault.retrieve (shrouded)
				asString <- unshrouded.extract[ErrorOr] (bs => new String (bs))
				} yield asString;

			Then ("what was retrieved is the same as what we vaulted");

			assert (result.forall (_ == original), result);
			}
		}
}
