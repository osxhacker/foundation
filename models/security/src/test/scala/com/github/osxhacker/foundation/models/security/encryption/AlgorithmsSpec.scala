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

package com.github.osxhacker.foundation.models.security.encryption

import scala.language.postfixOps

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.security.ProjectSpec


/**
 * The '''AlgorithmsSpec''' type defines the unit test suite which certify
 * [[com.github.osxhacker.foundation.models.security.encryption.Algorithm]] for fitness
 * of purpose.
 *
 * @author osxhacker
 *
 */
final class AlgorithmsSpec
	extends ProjectSpec
		with AlgorithmSupport
{
	"The AES encryption algorithm" must {
		"be resolvable in the implicit scope" in {
			val aes = implicitly[AES];
			
			assert (aes ne null);
			}
		
		"be usable in generic methods" in {
			val aes = algorithmFor[AES] ();

			assert (aes.algorithm.keySize > 0);
			}
		
		"generate encryption keys" in {
			val aes = algorithmFor[AES] ();
			val keys = aes.generator (KeyVersion (0));

			/// AES is a symmetric algorithm, so has no shared key.
			assert (keys.secret ne null);
			assert (keys.shared.isEmpty);
			}
		}
	
	"The RSA encryption algorithm" must {
		"be resolvable in the implicit scope" in {
			val rsa = implicitly[RSA];
			
			assert (rsa ne null);
			}
		
		"be usable in generic methods" in {
			val rsa = algorithmFor[RSA] ();

			assert (rsa.algorithm.keySize > 0);
			}
		
		"generate encryption keys" in {
			val rsa = algorithmFor[RSA] ();
			val keys = rsa.generator (KeyVersion (1));

			/// The RSA algorithm uses public and private keys.
			assert (keys.secret ne null);
			assert (keys.shared.isDefined);
			}
		}
}
