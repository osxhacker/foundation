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

import java.security.SecureRandom

import scala.language.{
	higherKinds,
	postfixOps
	}

import scala.util.Random

import akka.util.ByteString
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

import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''Salt''' type defines the Domain Object Model concept of a random
 * `String` used in one-way secure hashing.
 *
 * @author osxhacker
 * 
 * @see [[https://en.wikipedia.org/wiki/Salt_(cryptography)]]
 *
 */
final case class Salt (private val content : String)
{
	/**
	 * The externalized method attempts to produce a `M[String]` from
	 * '''this''' instance, producing an
	 * [[com.github.osxhacker.foundation.models.core.error.ApplicationError]] if unable.
	 */
	def externalized[M[_]] ()
		(implicit ME : MonadError[M, ApplicationError])
		: M[String] =
		ME.point (content);


	/**
	 * The toByteString method creates a [[akka.util.ByteString]] having the
	 * relevant `Byte`s needed to incorporate into a secure hash operation.
	 */
	def toByteString () : ByteString = ByteString (content.getBytes);
}


object Salt
{
	/// Class Types
	type DefaultLength = MinLength
	type MinLength = _10


	/// Instance Properties
	/// As of JDK-7, SecureRandom is thread safe.
	private val rng = new SecureRandom ();


	/**
	 * The random method produces a newly minted '''Salt''' having '''Length'''
	 * `alphanumeric` characters.  The '''Length''' must be at least
	 * `MinLength`, as enforced by the compiler.
	 */
	def random[Length <: Nat : ToInt] ()
		(implicit ev : GTEq[Length, MinLength])
		: Salt =
	{
		val random = new Random (rng);
		def chars : Stream[Char] = random.nextPrintableChar () #:: chars;

		return Salt (chars.take (Nat.toInt[Length]).mkString);
	}
}

