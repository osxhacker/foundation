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

package com.github.osxhacker.foundation.models.core

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import error._
import net.{
	Scheme,
	URN
	}


/**
 * The '''Identifier''' type captures the Domain Object Model concept of an
 * individual [[com.github.osxhacker.foundation.models.core]] type's unique
 * identifier.  Fundamental to an '''Identifier''' is that the `URN` is a
 * [[http://tools.ietf.org/html/rfc2141 Uniform Resource Name]].
 * 
 * For example, a valid '''Identifier''' could have the form:
 * 
 * {{{
 * "urn:some-entity:abc-123-xyz"
 * }}}
 *
 * An important thing to remember is that URN's are globally unique and
 * ''designate a name for all time''.  This implies no support for
 * heirarchical URN's.  See [[https://tools.ietf.org/html/rfc1630 here]]
 * for a detailed discussion regarding `URI`'s, `URL`'s, and `URN`'s.
 *
 * @author osxhacker
 *
 */
final case class Identifier (val urn : URN)
{
	/// Class Imports
	import syntax.equal._
	import syntax.std.boolean._


	/**
	 * The belongsTo method determines whether or not this '''Identifier'''
	 * orginated from the ''T''ype having a Domain Object Model specific
	 * [[com.github.osxhacker.foundation.models.core]] associated with it.
	 */
	def belongsTo[T <: AnyRef : Scheme] : Boolean =
		urn.nid === implicitly[Scheme[T]];


	/**
	 * The externalized method produces an `M[String]` representation of
	 * '''this''' instance, if possible.  Note that the content produced
	 * may have a different representation than what is held in the `urn`,
	 * depending on the [[com.github.osxhacker.foundation.models.core.net.Scheme]]'s
	 * `encoding`.
	 */
	def externalized[M[_]] (charset : String = "UTF-8")
		(implicit A : Applicative[M])
		: M[String] =
		A.point (
			"%s:%s".format (
				(Scheme.URN append urn.nid).name,
				urn.nid.encode (urn.nss, charset)
				)
			);


	/**
	 * The transferTo method produces an '''Identifier''' which has its
	 * [[com.github.osxhacker.foundation.models.core.net.URN]]'s
	 * [[com.github.osxhacker.foundation.models.core.net.Scheme]] the one for ''T'' while
	 * retaining the `nss` of '''this''' `urn`.
	 */
	def transferTo[T <: AnyRef : Scheme] () : Identifier =
		new Identifier (new URN (implicitly[Scheme[T]], urn.nss));


	/**
	 * The withoutScheme method provides the `urn.nss` component of '''this'''
	 * '''Identifier'''.  It exists to provide semantic value for when the
	 * `urn.nid` is not desired.
	 */
	def withoutScheme () : String = urn.nss;
}


object Identifier
{
	/// Class Imports
	import Scalaz._


	/// Class Types
	object Parts
	{
		def unapply (that : Any) : Option[(String, String)] =
			that match {
				case id : Identifier =>
					Some (id.urn.nid.name -> id.withoutScheme ());

				case URN (urn) =>
					Some (urn.nid.name -> urn.nss);

				case _ =>
					None
				}
	}
	
	
	/// Instance Properties
	private val parsingError : Throwable => ApplicationError =
		((e : Throwable) => LogicError ("invalid identifier", e));


	/**
	 * This apply method is defined to allow for functional-style creation
	 * of '''Identifier'''s from a String '''value'''.  Primarily, this method
	 * is for hydrating '''Identifier'''s previously externalized.
	 */
	def apply (value : String) : ApplicationError \/ Identifier =
		parsingError <-: URN (value) map (new Identifier (_));


	/**
	 * This version of the apply method allows the collaborator to provide
	 * the '''scheme''' independently of the '''value''' at run-time.
	 */
	def apply[T] (scheme : Scheme[T], value : String)
		: ApplicationError \/ Identifier =
	{
		val prefix = (Scheme.URN append scheme).name + ":";

		return apply (prefix + value.stripPrefix (prefix));
	}


	/// Implicit Conversions
	implicit val IdentifierEqual : Equal[Identifier] = Equal.equalA;
}

