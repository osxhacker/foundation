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

package com.github.osxhacker.foundation.models.security.scenario

import org.scalatest.DiagrammedAssertions
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.functional.ErrorOr
import com.github.osxhacker.foundation.models.security.ProjectSpec
import com.github.osxhacker.foundation.models.security.password._


/**
 * The '''InternalizePasswordSpec''' type defines the unit-tests which verify
 * the [[com.github.osxhacker.foundation.models.core.Scenario]]s involved in internalizing
 * [[com.github.osxhacker.foundation.models.security.password.Password]]s and serves as an
 * exemplar of their use.
 *
 * @author osxhacker
 *
 */
final class InternalizePasswordSpec ()
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import syntax.monad._


	"The InternalizePassword scenario" must {
		"require only the EncodedPassword to be constructed" in {
			val password = CleartextPassword ("a password") >>= {
				candidate =>

				CreatePassword (candidate).run ()
					.externalized[ErrorOr] ();
				}

			val scenario = password map {
				encoded =>

				InternalizePassword (encoded);
				}

			assert (scenario.isRight);
			}

		"produce a PasswordLike from a valid EncodedPassword" in {
			val clear = CleartextPassword ("a password");
			val password = clear >>= {
				candidate =>

				CreatePassword (candidate).run ()
					.externalized[ErrorOr] ();
				}

			val internalized = password >>= {
				encoded =>

				InternalizePassword (encoded).run ();
				}

			assert (internalized.isRight);
			assert (internalized.exists (_.matches (clear orThrow)));
			}
		}
}
