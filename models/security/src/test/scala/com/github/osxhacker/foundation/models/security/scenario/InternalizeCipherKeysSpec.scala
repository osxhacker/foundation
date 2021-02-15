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

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.text.encoding.Base64
import com.github.osxhacker.foundation.models.security.ProjectSpec
import com.github.osxhacker.foundation.models.security.encryption._


/**
 * The '''InternalizeCipherKeysSpec''' type defines the unit tests which
 * certify the
 * [[com.github.osxhacker.foundation.models.security.scenario.InternalizeCipherKeys]]
 * type for fitness of purpose.
 *
 * @author osxhacker
 *
 */
final class InternalizeCipherKeysSpec ()
	extends ProjectSpec
{
	/// Class Imports
	import syntax.all._


	"The InternalizeCipherKeys scenario" must {
		"be able to externalize AES CipherKeys" in {
			val generator = GenerateCipherKeys[AES] (KeyVersion (1));
			val original = generator ();
			val externalized = ExternalizeCipherKeys[AES] (original).run;
			val result = InternalizeCipherKeys[AES] (externalized).run;
			
			assert (result.isRight, result.toString);

			result foreach {
				cipherKeys =>

				assert (cipherKeys.version == cipherKeys.version);
				assert (cipherKeys.shared.isEmpty);
				assert (
					cipherKeys.secret.getEncoded.sameElements (
						original.secret.getEncoded
						),

					"secret keys differ"
					);

				assert (
					cipherKeys.initializationVector.get.sameElements (
						original.initializationVector.get
						),

					"initialization vectors differ: %s %s".format (
						original.initializationVector.map (_.mkString (",")),
						cipherKeys.initializationVector.map (_.mkString (","))
						)
					);
				}
			}
		}
}
