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

package com.github.osxhacker.foundation.models.security

import java.security.SecureRandom

import scala.language.higherKinds

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

import com.github.osxhacker.foundation.models.core.StringValidations
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError,
	InvalidModelStateError
	}

import com.github.osxhacker.foundation.models.core.text.encoding.{
	Decoder,
	Encoder
	}

import encryption.Algorithm
import vault.{
	Decrypted,
	Encrypted,
	Vaulted
	}


/**
 * The '''ConfirmationCode''' type defines the Domain Object Model reification
 * of a human-readable pseudo-random "code" provided to a person out-of-band
 * for the intent to verify their identity.
 *
 * @author osxhacker
 *
 */
final case class ConfirmationCode (val content : Vaulted[String])
{
	/// Class Imports
	import syntax.either._


	/**
	 * The externalized method attempts to produce a `M[String]` from
	 * '''this''' instance, producing an
	 * [[com.github.osxhacker.foundation.models.core.error.ApplicationError]] if unable.
	 */
	def externalized[M[_]] ()
		(implicit ME : MonadError[M, ApplicationError])
		: M[String] =
		content.extract[M] (bytes => new String (bytes))


	/**
	 * The toEncoded method produces a ''String'' containing the `content`
	 * of '''this''' instance if the `content` has been encrypted, failing
	 * otherwise.
	 */
	def toEncoded[E[X] <: Encoder[X]] ()
		(implicit e : E[Array[Byte]])
		: ApplicationError \/ String =
		content match {
			case encrypted : Encrypted[String] =>
				encrypted.toEncoded[E] ().right;

			case _ =>
				InvalidModelStateError ("confirmation code is not encrypted")
					.left;
			}
}


object ConfirmationCode
	extends StringValidations
{
	/// Class Types
	type DefaultLength = _5
	type MaxLength = _8
	type MinLength = _3


	/// Class Imports
	import syntax.either._
	import syntax.kleisli._
	import syntax.std.boolean._


	/// Instance Properties
	private val allowable = {
		val ignoredLetters = 'I' :: 'O' :: Nil;

		('2' to '9') ++ ('A' to 'Z').filterNot (ignoredLetters.contains);
		}

	/// As of JDK-7, SecureRandom is thread safe.
	private val rng = new SecureRandom ();


	/**
	 * This version of apply creates a '''ConfirmationCode''' having the
	 * ''DefaultLength'' for the system.
	 */
	def apply () : ConfirmationCode = random[DefaultLength] ();


	def apply (clear : String)
		: ApplicationError \/ ConfirmationCode =
		fromString[ConfirmationCode] {
			implicit v =>

			(
				notEmpty () >==>
				trim () >==>
				between (Nat.toInt[MinLength], Nat.toInt[MaxLength]) >==>
				andFinally {
					content =>

					val upper = content.toUpperCase ();

					upper.forall (allowable.contains).fold (
						new ConfirmationCode (Decrypted (upper)).right,
						DomainValueError (s"invalid code: '${clear}'").left
						);
					}
			).run (clear);
			}


	/**
	 * The random method produces a newly minted '''ConfirmationCode''' having
	 * '''Length''' `allowable` characters.  The '''Length''' must be at least
	 * `MinLength`, as enforced by the compiler.
	 */
	def random[Length <: Nat : ToInt] ()
		(implicit ev : GTEq[Length, MinLength])
		: ConfirmationCode =
		new ConfirmationCode (
			Decrypted (characters (Nat.toInt[Length]).mkString)
			);


	/**
	 * The internalize method is a model of the FACTORY pattern and will
	 * attempt to create a '''ConfirmationCode''' from a '''candidate'''
	 * previously externalized, such as when stored in a persistent
	 * store.
	 */
	def internalize[D[X] <: Decoder[X]] (candidate : String)
		(implicit d : D[Array[Byte]])
		: ApplicationError \/ ConfirmationCode =
		Encrypted.ParseOutputFormat[String, D] (candidate) map {
			content =>

			new ConfirmationCode (content);
			}


	private def characters (length : Int) : Stream[Char] =
		Stream.iterate (pick (), length) {
			_ =>

			pick ();
			}


	private def pick () : Char = allowable (rng.nextInt (allowable.length - 1))


	/// Implicit Conversions
	implicit val ConfirmationCodeEqual : Equal[ConfirmationCode] = Equal.equalA;
}
