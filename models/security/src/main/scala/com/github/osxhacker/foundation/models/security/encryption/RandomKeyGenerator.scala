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

import java.security.{
	KeyPairGenerator,
	SecureRandom
	}

import javax.crypto.KeyGenerator

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''RandomKeyGenerator''' trait defines the contract for producing
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]]s having randomly
 * produced keys.  There are two algorithm families addressed; symmetric and
 * public/private.
 *
 * @author osxhacker
 *
 */
sealed trait RandomKeyGenerator[A <: Algorithm[A]]
{
	/// Instance Properties
	protected def algorithm : A;
	
	
	/**
	 * The apply method will produce a
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] suitable for
	 * encrypting messages by ''A''.  The responsibility for persisting what
	 * is randomly created is not addressed at this level.
	 */
	def apply (version : KeyVersion) : CipherKeys;
}


object RandomKeyGenerator
{
	/**
	 * The apply method is provided so that a properly typed
	 * '''RandomKeyGenerator''' can be "summoned" as needed.
	 */
	def apply[A <: Algorithm[A]] ()
		(implicit rng : RandomKeyGenerator[A])
		: RandomKeyGenerator[A] = rng;


	/// Implicit Conversions
	implicit object AESKeyGenerator
		extends SymmetricKeyGenerator[AES]
	
	
	implicit object RSAKeyGenerator
		extends PublicPrivateKeyGenerator[RSA]
}


sealed abstract class SymmetricKeyGenerator[A <: Algorithm[A]] (
	implicit override val algorithm : A
	)
	extends RandomKeyGenerator[A]
{
	/// Instance Properties
	private val rng = new SecureRandom ();


	override def apply (version : KeyVersion) : CipherKeys =
	{
		val generator = KeyGenerator.getInstance (algorithm.name);
		
		generator.init (algorithm.keySize);

		val key = generator.generateKey ();
		val iv = new Array[Byte] (16);

		rng.nextBytes (iv);
		
		return CipherKeys (
			version,
			key,
			shared = None,
			initializationVector = Option (iv)
			);
	}
}


sealed abstract class PublicPrivateKeyGenerator[A <: Algorithm[A]] (
	implicit override val algorithm : A
	)
	extends RandomKeyGenerator[A]
{
	override def apply (version : KeyVersion) : CipherKeys =
	{
		val generator = KeyPairGenerator.getInstance (algorithm.name);

		generator.initialize (algorithm.keySize);

		val pair = generator.generateKeyPair ();

		return CipherKeys (
			version,
			pair.getPrivate,
			shared = Option (pair.getPublic),
			initializationVector = None
			);
	}
}
