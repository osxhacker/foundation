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

package com.github.osxhacker.foundation.models.security.vault

import java.util.Arrays

import scala.language.higherKinds

import scala.util.parsing.combinator._

import scalaz.{
	Failure => _,
	Id => _,
	Success => _,
	_
	}

import shapeless._

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError,
	LogicError
	}

import com.github.osxhacker.foundation.models.core.text.encoding.{
	Decoder,
	Encoder
	}

import com.github.osxhacker.foundation.models.security.encryption.{
	Algorithm,
	KeyVersion
	}


/**
 * The '''Vaulted''' trait defines a ''coproduct'' type which establishes a
 * total set of concrete types participating in securing arbitrary ''A''
 * instances which can be represented as an `Array[Byte]`.
 *
 * @author osxhacker
 *
 */
sealed trait Vaulted[A]
	extends Equals
{
	/// Instance Properties
	protected[vault] def content : Array[Byte];


	override def hashCode () : Int = Arrays.hashCode (content);


	/**
	 * The extract method attempts to produce an ''A'' from the managed
	 * '''content''' iff the concrete type allows unencrypted access.
	 */
	def extract[M[_]] (f : Array[Byte] => A)
		(implicit ME : MonadError[M, ApplicationError])
		: M[A];


	/**
	 * The vault method produces a version of '''this''' instance which is
	 * guaranteed to be encrypted only once.
	 */
	def vault[B <: Algorithm[B]] (vault : Vault[B]) : Encrypted[A];


	/**
	 * The unvault method produces a version of '''this''' instance which is
	 * guaranteed to be decrypted only once.
	 */
	def unvault[B <: Algorithm[B]] (vault : Vault[B]) : Decrypted[A];
}


/**
 * The '''Decrypted''' type defines the Domain Object Model class representing
 * unencrypted `content`.  As such, it ''does not'' have support for
 * encoding and/or transmission.
 * 
 * @author osxhacker
 */
final class Decrypted[A] (override protected[vault] val content : Array[Byte])
	extends Vaulted[A]
{
	override def canEqual (that : Any) : Boolean =
		that.isInstanceOf[Decrypted[_]];


	override def equals (that : Any) : Boolean =
		canEqual (that) &&
		Arrays.equals (content, that.asInstanceOf[Decrypted[_]].content);


	override def extract[M[_]] (f : Array[Byte] => A)
		(implicit ME : MonadError[M, ApplicationError])
		: M[A] =
		ME.point (f (content));


	override def vault[B <: Algorithm[B]] (vault : Vault[B])
		: Encrypted[A] =
		vault.store (this) valueOr (e => throw e);


	override def unvault[B <: Algorithm[B]] (vault : Vault[B])
		: Decrypted[A] =
		this;
}


object Decrypted
{
	def apply (content : Array[Byte]) : Decrypted[Array[Byte]] =
		new Decrypted[Array[Byte]] (content);


	def apply (content : String) : Decrypted[String] =
		new Decrypted[String] (content.getBytes);


	/// Implicit Conversions
	implicit def DecryptedGeneric[A] : Generic[Decrypted[A]] =
		new Generic[Decrypted[A]] {
			override type Repr = Array[Byte] :: HNil;

			override def from (list : Repr) : Decrypted[A] =
				new Decrypted[A] (list.head);

			override def to (a : Decrypted[A]) : Repr =
				a.content :: HNil;
			}
}


/**
 * The '''Encrypted''' type defines the Domain Object Model representation
 * of arbitrary `content` which ''has alreeady'' been "vaulted."  Being
 * relatively secure, it explicitly supports externalization via `toEncoded`.
 * 
 * @author osxhacker
 */
final class Encrypted[A] (
	val version : KeyVersion,
	override protected[vault] val content : Array[Byte]
	)
	extends Vaulted[A]
{
	override def canEqual (that : Any) : Boolean =
		that.isInstanceOf[Encrypted[_]];


	override def equals (that : Any) : Boolean =
		canEqual (that) &&
		Arrays.equals (content, that.asInstanceOf[Encrypted[_]].content);


	override def extract[M[_]] (f : Array[Byte] => A)
		(implicit ME : MonadError[M, ApplicationError])
		: M[A] =
		ME.raiseError (LogicError ("attempted access to encrypted content"));


	override def vault[B <: Algorithm[B]] (vault : Vault[B])
		: Encrypted[A] =
		this;


	override def unvault[B <: Algorithm[B]] (vault : Vault[B])
		: Decrypted[A] =
		vault.retrieve (this) valueOr (e => throw e);


	/**
	 * The toEncoded method produces a `String` from '''this''' which is
	 * suitable for transmission and/or storage with any collaborator
	 * that understands `String`s.  Since this only makes sense for an
	 * [[com.github.osxhacker.foundation.models.securing.vault.Encrypted]] instance,
	 * this operation is ''only'' available here.
	 */
	def toEncoded[E[X] <: Encoder[X]] ()
		(implicit e : E[Array[Byte]])
		: String =
		Encrypted.encode[E[Array[Byte]]] (version, content);
}


object Encrypted
{
	/// Class Types
	object ParseOutputFormat
		extends RegexParsers
	{
		/// Class Imports
		import std.string._
		import scalaz.syntax.either._
		import scalaz.syntax.equal._
		import scalaz.syntax.std.boolean._
		import scalaz.syntax.std.string._


		def apply[A, D[X] <: Decoder[X]] (value : String)
			(implicit e : D[Array[Byte]])
			: ApplicationError \/ Encrypted[A] =
			parseAll (spec[A, D], value) match {
				case error : NoSuccess =>
					DomainValueError (error.msg).left;
					
				case Success (enc, next) =>
					next.atEnd.fold (
						enc.right,
						DomainValueError (
							"invalid encrypted property format"
							).left
						);
				}


		private def encodedContent : Parser[String] = """.*""".r;


		private def keyVersion : Parser[Int] =
			"{" ~> """\d+""".r <~ "}" ^^ {
				Integer.parseInt
				}
		
		
		private def spec[A, D[X] <: Decoder[X]] (implicit d : D[Array[Byte]])
			: Parser[Encrypted[A]] =
			keyVersion ~ encodedContent ^^ {
				case version ~ encoded =>
					
				new Encrypted[A] (KeyVersion (version), d.decode (encoded));
				}
	}


	/// Instance Properties
	private val OutputFormat = "{%d}%s";


	/**
	 * The apply method is provided to support functional style creation
	 * of '''Encrypted''' instances.
	 */
	def apply (keyVersion : KeyVersion, content : Array[Byte])
		: Encrypted[Array[Byte]] =
		new Encrypted[Array[Byte]] (keyVersion, content);
	
	
	private def encode[E <: Encoder[Array[Byte]]] (
		version : KeyVersion,
		content : Array[Byte]
		)
		(implicit e : E)
		: String =
		OutputFormat.format (version.version, e.encode (content));


	/// Implicit Conversions
	implicit def EncryptedGeneric[A] : Generic[Encrypted[A]] =
		new Generic[Encrypted[A]] {
			override type Repr = KeyVersion :: Array[Byte] :: HNil;

			override def from (list : Repr) : Encrypted[A] =
				new Encrypted[A] (list.head, list.tail.head);

			override def to (a : Encrypted[A]) : Repr =
				a.version :: a.content :: HNil;
			}
}

