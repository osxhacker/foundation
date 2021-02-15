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

import java.util.Arrays

import scala.language.{
	higherKinds,
	postfixOps
	}

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

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError,
	InvalidModelStateError
	}

import com.github.osxhacker.foundation.models.core.text.encoding.{
	Decoder,
	Encoder
	}

import com.github.osxhacker.foundation.models.security.hash.{
	DigestAlgorithm,
	SecureHash
	}


/**
 * The '''EncodedPassword''' type is a model of VALUE OBJECT and serves to
 * reify both name and data type for each constituent property of a
 * [[com.github.osxhacker.foundation.models.security.password.OneWayPassword]].
 *
 * @author osxhacker
 *
 */
final case class EncodedPassword (
	val salt : String,
	val hash : String
	)


/**
 * The '''OneWayPassword''' trait defines the contract for
 * [[com.github.osxhacker.foundation.models.security.password.Password]]s such that
 * collaborators remain independent of parameterization.  Since
 * [[com.github.osxhacker.foundation.models.security.password.Password]]s are asymmetric,
 * the `trait` emparts that constraint semantically.
 *
 * @author osxhacker
 *
 */
sealed trait OneWayPassword
	extends Equals
{
	/// Instance Properties
	protected def algorithm : SecureHash;


	/**
	 * The externalized method attempts to produce a `M[EncodedPassword]` from
	 * '''this''' instance, producing an
	 * [[com.github.osxhacker.foundation.models.core.error.ApplicationError]] if unable.
	 * Where required, the
	 * [[com.github.osxhacker.foundation.models.core.text.encoding.Encoder]] specified
	 * will be used to produce the requisite `String`s.
	 */
	def externalized[M[_]] ()
		(implicit ME : MonadError[M, ApplicationError])
		: M[EncodedPassword];


	/**
	 * The matches method determines whether or not '''this''' instance
	 * computes the same one-way hash with its
	 * [[com.github.osxhacker.foundation.models.security.password.Salt]] and the given
	 * [[com.github.osxhacker.foundation.models.security.password.CleartextPassword]].
	 */
	def matches (clear : CleartextPassword) : Boolean;
}


object OneWayPassword
{
	/// Implicit Conversions
	implicit val OneWayPasswordEqual : Equal[OneWayPassword] = Equal.equalA;
}


/**
 * The '''Password''' type defines the Domain Object Model representation of
 * a secrete phrase used in verifying the identity of an external actor, such
 * as a customer or an entrant to a giveaway.
 *
 * @author osxhacker
 *
 */
final case class Password[A <: DigestAlgorithm] (
	private val salt : Salt,
	private val hash : Array[Byte]
	)
	(implicit private val da : A, encoder : Encoder[Array[Byte]])
	extends OneWayPassword
{
	/// Class Imports
	import syntax.monadError._


	/// Instance Properties
	override protected val algorithm = SecureHash[A] ();


	override def equals (that : Any) : Boolean =
		canEqual (that) &&
		algorithm.name == that.asInstanceOf[Password[A]].algorithm.name &&
		Arrays.equals (hash, that.asInstanceOf[Password[A]].hash);


	override def externalized[M[_]] ()
		(implicit ME : MonadError[M, ApplicationError])
		: M[EncodedPassword] =
		salt.externalized[M] () >>= {
			theSalt =>

			EncodedPassword (theSalt, encoder.encode (hash)).point[M];
			}


	override def hashCode () : Int = Arrays.hashCode (hash);


	override def matches (clear : CleartextPassword) : Boolean =
		Arrays.equals (
			hash,
			algorithm.digest (salt.toByteString () ++ clear.toByteString ())
			);


	override def toString () : String =
		"Password(%s,%s)".format (salt, "X" * hash.length);
}


object Password
{
	/// Class Imports
	import \/.fromTryCatchThrowable
	import syntax.either._
	import syntax.std.boolean._


	/// Instance Properties
	private val decodeFailed : Throwable => ApplicationError =
		DomainValueError ("unable to parse password", _);


	/**
	 * This apply method mints a '''Password''' by using a `random`
	 * [[com.github.osxhacker.foundation.models.security.password.Salt]] and the given
	 * '''clear''' instance.
	 */
	def apply[
		A <: DigestAlgorithm,
		E[X] <: Encoder[X],
		Length <: Nat
		] (clear : CleartextPassword)
		(
			implicit da : A,
			ev : GTEq[Length, Salt.MinLength], ti : ToInt[Length],
			encoder : E[Array[Byte]]
		)
		: Password[A] =
		apply[A, E] (Salt.random[Length] (), clear);


	/**
	 * This apply method mints a '''Password''' by using a pre-existing
	 * [[com.github.osxhacker.foundation.models.security.password.Salt]] and the gven
	 * '''clear''' instance.
	 */
	def apply[A <: DigestAlgorithm, E[X] <: Encoder[X]] (
		salt : Salt,
		clear : CleartextPassword
		)
		(implicit da : A, encoder : E[Array[Byte]])
		: Password[A] =
	{
		val hasher = SecureHash[A] ();

		return new Password[A] (
			salt,
			hasher.digest (salt.toByteString () ++ clear.toByteString ())
			);
	}


	/**
	 * The internalize method attempts to create a '''Password''' from an
	 * existing
	 * [[com.github.osxhacker.foundation.models.security.password.EncodedPassword]],
	 * wrapping any resultant exception in a context-specific
	 * [[com.github.osxhacker.foundation.models.core.error.DomainValueError]].
	 */
	def internalize[
		A <: DigestAlgorithm,
		ED[X] <: Decoder[X] with Encoder[X]
		] (encoded : EncodedPassword)
		(implicit da : A, d : ED[Array[Byte]])
		: ApplicationError \/ Password[A] =
		fromTryCatchThrowable[Array[Byte], Throwable] {
			d.decode (encoded.hash);
			}
			.bimap (
				decodeFailed,
				Password[A] (Salt (encoded.salt), _)
				);


	/// Implicit Conversions
	implicit def PasswordEqual[A <: DigestAlgorithm] : Equal[Password[A]] =
		Equal.equalA;
}
