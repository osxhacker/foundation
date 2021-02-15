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


/**
 * The '''Algorithm''' trait defines encryption-specific behavior, including
 * how each is selected, initialized, and their respective private keys are
 * produced.
 *
 * @author osxhacker
 *
 */
sealed trait Algorithm[A <: Algorithm[A]]
{
	/// Class Imports


	/// Class Types


	/// Instance Properties
	/**
	 * Each '''Algorithm''' specifies the size, in bits, of the keys it uses.
	 */
	val keySize : Int;
	
	/**
	 * The name property identifies the symbolic name of the cipher, such as
	 * "AES" or "RSA".
	 * 
	 * @see https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher
	 */
	val name : String;

	/**
	 * The transformation property specifies the cipher `name`, mode, and
	 * padding parameters.
	 * 
	 * @see https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher
	 */
	val transformation : String;
}


object Algorithm
{
	/**
	 * The apply method is provided so that an ''A'' can be "summoned" as
	 * needed.
	 */
	def apply[A <: Algorithm[A]] (implicit a : A) : A = a;


	/// Implicit Conversions
	implicit object AESSupported
		extends AES
		
		
	implicit object RSASupported
		extends RSA
}


sealed trait AES
	extends Algorithm[AES]
{
	/// Instance Properties
	override val keySize = 128;
	override val name = "AES";
	override val transformation = "AES/CBC/PKCS5Padding";
}


sealed trait RSA
	extends Algorithm[RSA]
{
	/// Instance Properties
	override val keySize = 1024;
	override val name = "RSA";
	override val transformation = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";
}
