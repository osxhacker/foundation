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

package com.github.osxhacker.foundation.models.core.net

import java.net.URLEncoder

import scala.util.Try

import scalaz._


sealed trait SchemeEncoding
{
	def encode (content : String, characterSet : String) : String;
}


case object EscapeContent
	extends SchemeEncoding
{
	def encode (content : String) : String = encode (content, "UTF-8");

	override def encode (content : String, characterSet : String) : String =
		URLEncoder.encode (content, characterSet);
}


case object RawContent
	extends SchemeEncoding
{
	override def encode (content : String, characterSet : String) : String =
		content;
}


/**
 * The '''Scheme''' type reifies the [[http://tools.ietf.org/html/rfc3986 URI]]
 * concept of a category identifiers.  From the specification:
 * 
 * Each URI begins with a scheme name that refers to a specification for
 * assigning identifiers within that scheme.  As such, the URI syntax is
 * a federated and extensible naming system wherein each scheme's
 * specification may further restrict the syntax and semantics of
 * identifiers using that scheme.
 *
 * @author osxhacker
 *
 */
final case class Scheme[+A] (
	val name : String,
	private val encoding : SchemeEncoding
	)
{
	/// Class Imports
	import syntax.std.boolean._


	override def equals (other : Any) : Boolean =
		canEqual (other) &&
		other.asInstanceOf[Scheme[A]].name.equalsIgnoreCase (name);


	override def hashCode : Int = name.toLowerCase.hashCode ();


	/**
	 * The append method produces a
	 * [[com.github.osxhacker.foundation.models.core.net.Scheme]] consisting of this
	 * `name` and the `other` `name` separated by a period.  According to the
	 * [[https://tools.ietf.org/html/rfc3986#section-3.1 URI Scheme]]
	 * requirements, the '''Scheme''' produced must not contain a colon unless
	 * '''this''' '''Scheme''' is `urn`.
	 */
	def append[AA >: A] (other : Scheme[AA]) : Scheme[AA] =
		new Scheme[AA] (
			this.name + separatorFor (this.name) + other.name,
			this.encoding
			);


	/**
	 * The encode method potentially ''escapes'' the '''content''' so that it
	 * conforms to HTTP semantics.
	 */
	def encode (content : String, characterSet : String) : String =
		encoding.encode (content, characterSet);


	private def separatorFor (nid : String) : String =
		nid.equalsIgnoreCase (Scheme.URN.name).fold (":", ".");
}


object Scheme
{
	/// Class Imports
	import std.option._
	import syntax.monad._
	import syntax.std.boolean._


	/// Instance Properties
	val OID = Scheme[Nothing] ("oid", EscapeContent);
	val URN = Scheme[Nothing] ("urn", EscapeContent);


	def apply[A] (name : String) : Scheme[A] =
		new Scheme[A] (name.toLowerCase, EscapeContent);


	/// Implicit Conversions
	implicit def SchemeEqual[A] : Equal[Scheme[A]] =
		new Equal[Scheme[A]] {
			override def equal (a : Scheme[A], b : Scheme[A]) : Boolean =
				a.name.equalsIgnoreCase (b.name);
			}


	implicit def SchemeSemigroup[A] : Semigroup[Scheme[A]] =
		new Semigroup[Scheme[A]] {
			override def append (a : Scheme[A], b : => Scheme[A]) : Scheme[A] =
				a append (b);
			}
}

