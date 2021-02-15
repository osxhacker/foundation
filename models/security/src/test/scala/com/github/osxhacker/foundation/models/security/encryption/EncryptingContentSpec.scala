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

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.BehaviorSpec


/**
 * The '''EncryptingContentSpec''' type defines the requirements for being able
 * to encrypt arbitrary content.
 *
 * @author osxhacker
 *
 */
final class EncryptingContentSpec
	extends BehaviorSpec
		with AlgorithmSupport
{
	/// Class Imports
	import scalaz.syntax.monad._


	/// Class Types


	/// Instance Properties


	info ("The primary purpose of the encryption package is to encrypt and");
	info ("decrypt arbitrary content (such as text, numerics, and ADT's)");


	feature ("AES symmetric encryption") {
		val aes = algorithmFor[AES] ();

		scenario ("Encrypting strings") {
			Given ("a KeyPair assumed to have previously been loaded/defined");

			val keys = aes.generator (KeyVersion (0));

			And ("plain-text to encrypt");

			val original = "super secret info!";
			
			When ("encrypted");

			val encrypted = new SinglePartEncryption[AES] (keys).apply (
				original.getBytes
				);
			
			Then ("the bytes produce will not match the original");

			assert (encrypted.isRight);
			encrypted foreach {
				bytes =>

				assert (!original.getBytes.sameElements (bytes));
				}

			And ("the contents can be decrypted");

			val decrypt = new SinglePartDecryption[AES] (keys);
			val result = encrypted >>= (decrypt.apply _);

			assert (result.isRight, result.toString);
			result map (b => new String (b, "UTF-8")) foreach {
				decryptedContent =>
					
				assert (original == decryptedContent);
				}
			}
		}
}
