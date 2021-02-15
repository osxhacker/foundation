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

import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.security.error._


/**
 * The '''SinglePartDecryption''' type provides decryption of arbitrary
 * content which has a known size, using the
 * [[com.github.osxhacker.foundation.models.security.encryption.Algorithm]] specified.
 * Note that '''every''' decryption is `synchronized` since
 * [[javax.crypto.Cipher]] is __not__ thread-safe.
 *
 * @author osxhacker
 *
 */
final class SinglePartDecryption[A <: Algorithm[A]] (
	private val keys : CipherKeys
	)
	(implicit algorithm : A)
{
	/// Class Imports
	import \/.fromTryCatchThrowable
	import scalaz.syntax.bifunctor._
	import scalaz.syntax.bind._
	import scalaz.syntax.either._
	import scalaz.syntax.std.option._


	/// Instance Properties
	private val invalidKey : GeneralSecurityException => SecurityError =
		ex => InvalidDecryptionKeyError (
			s"Decryption key invalid for ${algorithm.transformation}",
			ex
			);
		
	private val cipher = init (keys);


	def apply (input : Array[Byte])
		: SecurityError \/ Array[Byte] =
		synchronized (cipher >>= decrypt (input));


	private def decrypt (input : Array[Byte])
		(cipher : Cipher)
		: SecurityError \/ Array[Byte] =
		cipher.doFinal (input).right;


	private def init (keys : CipherKeys) : SecurityError \/ Cipher =
		invalidKey <-: fromTryCatchThrowable[Cipher, GeneralSecurityException] {
			val cipher = Cipher.getInstance (algorithm.transformation);
			
			keys.initializationVector map (new IvParameterSpec (_)) cata (
				iv => cipher.init (Cipher.DECRYPT_MODE, keys.secret, iv),
				cipher.init (Cipher.DECRYPT_MODE, keys.secret)
				);
			
			cipher;
			}
}

