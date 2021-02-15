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

package com.github.osxhacker.foundation.models.security.password

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

import shapeless.ops.nat._
import shapeless.nat._

import com.github.osxhacker.foundation.models.core.akkax.ActorBasedSpec
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional.ErrorOr
import com.github.osxhacker.foundation.models.core.text.encoding._
import com.github.osxhacker.foundation.models.security.hash._


/**
 * The '''PasswordSpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.security.password.Password]] Domain Object
 * Model type for fitness of purpose and also serve as an exemplar of its use.
 *
 * @author osxhacker
 *
 */
final class PasswordSpec
	extends ActorBasedSpec ("test-password")
		with DiagrammedAssertions
		with WordSpecLike
{
	/// Class Imports
	import syntax.all._


	"A Password" must {
		"be constructable from an unknown clear-text password" in {
			val result = CleartextPassword ("some text") map {
				Password[SHA1, Base32, _10] (_);
				}

			assert (result.isRight);
			}

		"reject empty clear-text values" in {
			val result = CleartextPassword ("") map {
				Password[SHA1, Base64, _10] (_);
				}

			assert (result.isLeft, "expected empty string to be rejected");
			}

		"support logical equality" in {
			val salt = Salt.random[_11] ();
			val clear = CleartextPassword ("some text");
			val first = clear map (Password[MD5, Hex] (salt, _));
			val second = clear map (Password[MD5, Hex] (salt, _));

			assert (first.isRight);
			assert (second.isRight);
			assert (first == second);
			assert (first.map (_.hashCode ()) == second.map (_.hashCode ()));
			}

		"support parsing encoded content" in {
			val source = CleartextPassword ("secret!") map {
				Password[SHA256, Base32, _10] (_)
				}

			val encoded = source >>= (_.externalized[ErrorOr] ());

			assert (encoded.isRight, encoded.toString);

			val parsed = encoded >>= Password.internalize[SHA256, Base32];

			assert (parsed.isRight, parsed.toString);
			assert (parsed == source);
		}

		"be inequal when having different salts" in {
			val clear = CleartextPassword ("some text");
			val first = clear map (Password[MD5, Hex, _20] (_));
			val second = clear map (Password[MD5, Hex, _20] (_));

			assert (first.isRight);
			assert (second.isRight);
			assert (first != second);
			assert (first.map (_.hashCode ()) != second.map (_.hashCode ()));
			}

		"be equal when using different encoders" in {
			val salt = Salt.random[_11] ();
			val clear = CleartextPassword ("some text");
			val first = clear map (Password[SHA256, Hex] (salt, _));
			val second = clear map (Password[SHA256, Base64] (salt, _));

			assert (first.isRight);
			assert (second.isRight);
			assert (first == second);
			assert (first.map (_.hashCode ()) == second.map (_.hashCode ()));
			}

		"be inequal when using different secure hash algorithms" in {
			val salt = Salt.random[_11] ();
			val clear = CleartextPassword ("some text");
			val first = clear map (Password[MD5, Hex] (salt, _));
			val second = clear map (Password[SHA256, Base64] (salt, _));

			assert (first.isRight);
			assert (second.isRight);
			assert (first != second);
			assert (first.map (_.hashCode ()) != second.map (_.hashCode ()));
			}
	}
}

