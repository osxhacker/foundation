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

package com.github.osxhacker.foundation.models.security.hash

import java.security._

import akka.util.ByteString
import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''SecureHash''' type type defines the contract for all hashing
 * algorithms used in authentication mechanisms for external actors.
 *
 * @author osxhacker
 *
 */
sealed trait SecureHash
{
	/// Instance Properties
	val name : String;


	/**
	 * The digest method produces an `Array[Byte]` digest from the given
	 * '''source'''.
	 */
	def digest (source : Array[Byte]) : Array[Byte];
	
	
	/**
	 * This digest method produces an `Array[Byte]` digest by `compact`ing
	 * '''source''' and delegating to the `Array[Byte]` method.
	 */
	final def digest (source : ByteString) : Array[Byte] =
		digest (source.compact.toArray);


	/**
	 * This digest method produces an `Array[Byte]` digest by consuming all
	 * `Byte`s produced by the given '''stream'''.  Note that this implies use
	 * of an infinite `Stream` will never return.
	 */
	final def digest (source : Stream[Byte]) : Array[Byte] =
		digest (source.toArray);
	
	
	/**
	 * This version of digest is provided for syntactic convenience and
	 * delegates to the `Array[Byte]` method.
	 */
	final def digest (source : String) : Array[Byte] =
		digest (source.getBytes);
}


object SecureHash
{
	/// Class Imports
	import scalaz.std.string._
	import scalaz.syntax.equal._


	/**
	 * The apply method allows callers to produce a '''SecureHash''' which
	 * uses the desired
	 * [[com.github.osxhacker.foundation.models.security.hash.DigestAlgorithm]] in its
	 * operation.
	 */
	def apply[A <: DigestAlgorithm] ()
		(implicit algorithm : A)
		: SecureHash =
		new SecureHash {
			override val name = algorithm.name;
			override def digest (source : Array[Byte]) : Array[Byte] =
				algorithm.computeDigest (source);
			}


	/// Implicit Conversions
	implicit val SecureHashEqual : Equal[SecureHash] =
		Equal.equal[SecureHash] ((a, b) => a.name === b.name);
}


/**
 * The '''DigestAlgorithm''' Defines a ''type class'' that encodes one specific
 * [[com.github.osxhacker.foundation.models.security.hash.SecureHash]] algorithm.
 *
 * @author osxhacker
 *
 */
sealed trait DigestAlgorithm
	extends Equals
{
	/// Instance Properties
	/**
	 * Each '''DigestAlgorithm''' must have a unique name associated with it.
	 */
	val name : String;


	/**
	 * The computeDigest method produces a message digest from the '''source'''
	 * `Stream[Byte]`.
	 */
	def computeDigest (source : Array[Byte]) : Array[Byte];
}


trait MD5
	extends DigestAlgorithm


trait SHA1
	extends DigestAlgorithm


trait SHA256
	extends DigestAlgorithm


object DigestAlgorithm
{
	/// Class Imports
	import std.string._
	import syntax.equal._


	/// Class Types
	sealed abstract class MessageDigestAlgorithm[T <: DigestAlgorithm] (
		override val name : String
		)
	{
		/// Self Type Constraints
		this : T =>


		override def canEqual (that : Any) : Boolean =
			that.isInstanceOf[DigestAlgorithm];


		override def computeDigest (source : Array[Byte]) : Array[Byte] =
		{
			val md5Digester = MessageDigest.getInstance (name);

			return (md5Digester.digest (source));
		}


		override def equals (that : Any) : Boolean =
			canEqual (that) && that.asInstanceOf[DigestAlgorithm].name == name;
	}
	
	
	implicit object MD5
		extends MessageDigestAlgorithm[MD5] ("MD5")
			with MD5
	
	
	implicit object SHA1
		extends MessageDigestAlgorithm[SHA1] ("SHA-1")
			with SHA1
	
	
	implicit object SHA256
		extends MessageDigestAlgorithm[SHA256] ("SHA-256")
			with SHA256


	/// Implicit Conversions
	implicit def DigestAlgorithmEqual : Equal[DigestAlgorithm] =
		Equal.equal[DigestAlgorithm] ((a, b) => a.name === b.name);
}
