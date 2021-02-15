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

package com.github.osxhacker.foundation.models.security

import scala.language.postfixOps

import org.scalatest.{
	DiagrammedAssertions,
	WordSpecLike
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import shapeless.{
	syntax => _,
	_
	}

import shapeless.nat._

import com.github.osxhacker.foundation.models.core.akkax.ActorBasedSpec
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional.ErrorOr

import encryption.AES
import vault._


/**
 * The '''ConfirmationCodeSpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.security.ConfirmationCode]] type for fitness
 * of purpose.
 *
 * @author osxhacker
 *
 */
final class ConfirmationCodeSpec
	extends ActorBasedSpec ("test-confirmation-code")
		with VaultSupport
		with WordSpecLike
		with DiagrammedAssertions
{
	/// Class Imports
	import syntax.all._


	/// Instance Properties
	implicit val keyStorage = MockKeyStorage[AES] ();
	lazy val safe = VaultFactory ("test-confirmation-code").apply () orThrow;


	"A ConfirmationCode" must {
		"support a default compile-time length" in {
			val code = ConfirmationCode ();
			val externalized = code.externalized[ErrorOr] ();
			val defaultLength = Nat.toInt[ConfirmationCode.DefaultLength];

			assert (externalized.isRight);
			assert (externalized.exists ( _.length === defaultLength));
			}

		"suppoort a specified compile-time length" in {
			val code = ConfirmationCode.random[_4] ();
			val externalized = code.externalized[ErrorOr] ();

			assert (externalized.isRight);
			assert (externalized.exists (_.length === Nat.toInt[_4]));
			}

		"support validated clear-text construction" in {
			val code = ConfirmationCode ("ABC234");
			val externalized = code >>= (_.externalized[ErrorOr] ());

			assert (code.isRight);
			assert (externalized.exists (_.length === Nat.toInt[_6]));
			}

		"be case insensitive during clear-text construction" in {
			val lower = ConfirmationCode ("abcde");
			val upper = ConfirmationCode ("ABCDE");

			assert (lower.isRight);
			assert (upper.isRight);
			assert (lower == upper);
			assert (lower.hashCode () == upper.hashCode ());
			}

		"detect invalid clear-text content" in {
			val badChars = ConfirmationCode ("1oO($!");
			val tooLong = ConfirmationCode ("ABcde3456789");

			assert (badChars.isLeft);
			assert (tooLong.isLeft);
			}

		"detect missing clear-text content" in {
			val empty = ConfirmationCode ("");
			val invalid = ConfirmationCode (null : String);

			assert (empty.isLeft);
			assert (invalid.isLeft);
			}

		"produce distinct random instances" in {
			val (first, second) = (
				ConfirmationCode ().externalized[ErrorOr] (),
				ConfirmationCode ().externalized[ErrorOr] ()
				);

			assert (first.isRight, first.toString);
			assert (second.isRight, second.toString);
			assert (first != second);
			}
		}
}
