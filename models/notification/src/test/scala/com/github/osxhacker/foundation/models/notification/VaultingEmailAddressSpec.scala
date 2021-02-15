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

package com.github.osxhacker.foundation.models.notification

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.BehaviorSpecLike
import com.github.osxhacker.foundation.models.core.akkax.ActorBasedSpec
import com.github.osxhacker.foundation.models.core.text.encoding.Base32
import com.github.osxhacker.foundation.models.security.encryption.AES
import com.github.osxhacker.foundation.models.security.hash.MD5
import com.github.osxhacker.foundation.models.security.vault._


/**
 * The '''VaultingEmailAddressSpec''' type defines the behaviour expected when
 * an [[com.github.osxhacker.foundation.models.notification.EmailAddress]] is `store`d in
 * a [[com.github.osxhacker.foundation.models.security.vault.Vault]].
 *
 * @author osxhacker
 *
 */
final class VaultingEmailAddressSpec
	extends ActorBasedSpec ("test-vaulting-email-address")
		with BehaviorSpecLike
		with VaultSupport
{
	/// Class Imports
	import syntax.all._


	/// Instance Properties
	implicit val keyStorage = MockKeyStorage[AES] ();
	lazy val safe = VaultFactory ("vault-email").apply ();


	info ("An EmailAddress must support vaulting so that its contents can be");
	info ("securely managed both in a persistent store and with collaborators");

	feature ("Storing into a Vault") {
		scenario ("Encrypting a clear-text EmailAddress") {
			Given ("an unencrypted email address instance");

			val original = EmailAddress[MD5, Base32] ("bob", "example.com");

			assert (!original.hash.isEmpty);

			When ("a Vault is instructed to 'store' it");

			val stored = safe >>= (_.store (original));

			Then ("the operation should succeed");
			assert (stored.isRight);

			And ("the stored instance should contain an Encrypted address");
			assert (stored.exists (_.address.isInstanceOf[Encrypted[String]]));
			}

		scenario ("Encrypting an already encrypted EmailAddress") {
			Given ("an encrypted email address");

			val original = safe >>= {
				_.store (EmailAddress[MD5, Base32] ("bob", "example.com"));
				}

			When ("it is vaulted again");

			val result = for {
				s <- safe
				instance <- original
				stored <- s.store (instance)
				} yield stored;

			Then ("the result should be an idempotent operation");
			assert (result.isRight === original.isRight);
			assert (result.map (_.hash) == original.map (_.hash));
			}
		}

	feature ("Retrieving from a Vault") {
		scenario ("Decrypting an encrypted EmailAddress") {
			Given ("an encrypted email address");

			val original = safe >>= {
				_.store (EmailAddress[MD5, Base32] ("bob", "example.com"));
				}

			When ("it is retrieved from a Vault");

			val result = for {
				s <- safe
				instance <- original
				retrieved <- s.retrieve (instance)
				} yield retrieved;

			Then ("the request should succeed");
			assert (result.isRight);

			And ("the EmailAddress contents should be decrypted");
			assert (
				result.exists (_.address.isInstanceOf[Decrypted[String]]),
				s"expected result to be decrypted: ${result}"
				);
			}
		}
}
