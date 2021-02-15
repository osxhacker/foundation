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

package com.github.osxhacker.foundation.models.notification

import javax.mail.internet.InternetAddress

import scala.language.higherKinds

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import shapeless.Generic

import com.github.osxhacker.foundation.models.core.Identifier
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError,
	InvalidModelStateError
	}

import com.github.osxhacker.foundation.models.core.functional.ErrorOr
import com.github.osxhacker.foundation.models.core.net.Scheme
import com.github.osxhacker.foundation.models.core.text.encoding.{
	Decoder,
	Encoder
	}

import com.github.osxhacker.foundation.models.security.hash.{
	DigestAlgorithm,
	MD5,
	SecureHash
	}

import com.github.osxhacker.foundation.models.security.vault.{
	Decrypted,
	Encrypted,
	Vaulted
	}


/**
 * The '''EmailAddress''' type defines the Domain Object Model representation
 * of an
 * [[https://tools.ietf.org/html/rfc5322#section-3.4 IETF Address Specification]].
 *
 * @author osxhacker
 *
 */
final case class EmailAddress (
	val address : Vaulted[String],
	val hash : String
	)
{
	/// Class Imports
	import std.string._
	import syntax.either._
	import syntax.equal._
	import syntax.std.string._


	/// Instance Properties
	/**
	 * The sanitized property ensures that `hash` can be used as the NSS
	 * component of an [[com.github.osxhacker.foundation.models.core.Identifier]].
	 * For information regarding the values used, see the
	 * [[https://tools.ietf.org/html/rfc2141 Namespace Specific String Syntax]].
	 */
	private lazy val sanitized = hash.replace ('/', '$')
		.replace ('=', '$');


	/**
	 * The equals method is overridden to ensure that the '''hash'''
	 * property is the sole determiner of logical equality.
	 */
	override def equals (that : Any) : Boolean =
		canEqual (that) &&
			that.asInstanceOf[EmailAddress].hash === hash;


	/**
	 * Since `equals` is altered from its default implementation, hashCode is
	 * defined using the same properties involved in determining logical
	 * equality so that use with associative containers has predictable
	 * behaviour.
	 */
	override def hashCode () : Int = hash.hashCode;


	/**
	 * The toEncoded method produces a ''Tuple2'' containing the `address`
	 * and `hash` of '''this''' instance if the `address` has been
	 * encrypted, failing otherwise.
	 */
	def toEncoded[E[X] <: Encoder[X]] ()
		(implicit e : E[Array[Byte]])
		: ApplicationError \/ (String, String) =
		address match {
			case encrypted : Encrypted[String] =>
				(encrypted.toEncoded[E] (), hash).right;

			case _ =>
				InvalidModelStateError ("email address is not encrypted").left;
			}


	/**
	 * The toIdentifier method produces a stable
	 * [[com.github.osxhacker.foundation.models.core.Identifier]] from the sanitized
	 * contents of the `hash`.
	 */
	def toIdentifier[A] ()
		(implicit scheme : Scheme[A])
		: ApplicationError \/ Identifier =
		Identifier (scheme, sanitized);


	/**
	 * The toInternetAddress method produces a
	 * [[javax.mail.internet.InternetAddress]] from '''this''' instance or
	 * fails if the `address` is
	 * [[com.github.osxhacker.foundation.models.security.vault.Encrypted]].
	 */
	def toInternetAddress ()
		: ApplicationError \/ InternetAddress =
		address.extract[ErrorOr] (bytes => new String (bytes, "UTF-8")) map {
			addr =>

			new InternetAddress (addr);
			}
}


object EmailAddress
{
	/// Class Imports
	import std.string._
	import syntax.either._


	/**
	 * The apply method is provided to support functional syntax for creating
	 * '''EmailAddress''' instances with __valid__ `local` and `domain`
	 * components.
	 */
	def apply[A <: DigestAlgorithm, E[X] <: Encoder[X]] (
		local : String,
		domain : String
		)
		(implicit da : A, e : E[Array[Byte]])
		: EmailAddress =
	{
		val address = local.toLowerCase + "@" + domain.toLowerCase;
		
		return new EmailAddress (
			Decrypted (address),
			e.encode (SecureHash[A] ().digest (address))
			);
	}
	
	
	/**
	 * The internalize method is a model of the FACTORY pattern and will
	 * attempt to create an '''EmailAddress''' from a '''candidate''' and
	 * '''hash''' previously externalized, such as when stored in a persistent
	 * store.
	 */
	def internalize[D[X] <: Decoder[X]] (candidate : String, hash : String)
		(implicit d : D[Array[Byte]])
		: ApplicationError \/ EmailAddress =
		Encrypted.ParseOutputFormat[String, D] (candidate) bimap (
			DomainValueError ("invalid email address", _),
			address => new EmailAddress (address, hash)
			);


	/// Implicit Conversions
	implicit val EmailAddressEqual : Equal[EmailAddress] =
		Equal.equalBy (_.hash);
}

