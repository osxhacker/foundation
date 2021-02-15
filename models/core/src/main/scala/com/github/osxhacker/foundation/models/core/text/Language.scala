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

package com.github.osxhacker.foundation.models.core.text

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.StringValidations
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError
	}


/**
 * The '''Language''' type defines the Domain Object Model representation of
 * [[https://tools.ietf.org/html/rfc5646 RFC-5646]] "IETF Tags for Identifying
 * Languages", as it applies to the system.  There terminology used here can be
 * found in [[https://tools.ietf.org/html/rfc5646#section-2.1 Section 2.1]] and
 * are briefly described as:
 * 
 *   - '''tag''': the complete '''Languages''' tag.
 * 
 *   - '''primary''': the first subtag within `tag` and corresponds to the
 * natural language, such as 'en' for English and 'fr' for French.
 * 
 *   - '''region''': optional second subtag which identifies a particular
 * dialect, such as 'US' for 'en-US' and 'GB' for 'en-GB'.
 *
 * @author osxhacker
 *
 */
final case class Language (
	val tag : String,
	val primary : String,
	val region : Option[String]
	)
	extends Ordered[Language]
{
	/// Class Imports
	import std.string._
	import syntax.applicative._
	import syntax.equal._
	import syntax.std.option._


	/// Instance Properties
	private lazy val normalized = "%s%s".format (
		primary.toLowerCase (),
		region map ("-" + _.toUpperCase ()) orZero
		);


	override def compare (other : Language) : Int =
		tag.toLowerCase ().compareTo (other.tag.toLowerCase ());


	override def equals (that : Any) : Boolean =
		canEqual (that) && tag.toLowerCase ().equals (
			that.asInstanceOf[Language].tag.toLowerCase ()
			);


	override def hashCode () : Int = tag.toLowerCase ().hashCode ();


	/**
	 * The contains method determines if '''this''' instance is either the same
	 * as the '''other''' __or__ if both have the same `primary` and '''this'''
	 * `region` is not specified.  For example:
	 * 
	 * {{{
	 * Language ("en").contains (Language ("en-US")) === true;
	 * Language ("en-GB").contains (Language ("en")) === false;
	 * }}}
	 */
	def contains (other : Language) : Boolean =
		(tag === other.tag) || (region.isEmpty && primary === other.primary);


	/**
	 * The externalized method produces an `M[String]` representation of
	 * '''this''' instance in canonical form, if possible.
	 */
	def externalized[M[_]] ()
		(implicit A : Applicative[M])
		: M[String] =
		A.point (normalized);
}


object Language
	extends StringValidations
{
	/// Class Imports
	import Id.Id
	import format.Textualize
	import std.string._
	import syntax.all._
	import syntax.std.boolean._


	/// Instance Properties
	val default : Language = Language ("en", "en", None);
	private val ValidFormat = """^\w{2,3}(?:-\w{2,4})?$""".r;


	/**
	 * This version of the apply method parses an arbitrary '''candidate''' and
	 * ensures it adheres to the expected format (ignoring leading and/or
	 * trailing whitespaces, if any).
	 */
	def apply (candidate : String) : ApplicationError \/ Language =
		fromString[Language] {
			implicit v =>

			(
				notEmpty () >==>
				trim () >==>
				between (2, 8) >==>
				matching (ValidFormat)
			).run (candidate) map (parse);
			}


	private def parse : String => Language =
		tag => {
			val parts = tag.split ('-');

			Language (
				tag,
				parts (0),
				(parts.length == 2) option (parts (1))
				);
			}


	/// Implicit Conversions
	implicit val LanguageEqual : Equal[Language] =
		Equal.equalBy (_.tag.toLowerCase ());

	implicit val LanguageShow : Show[Language] = Show.showFromToString;

	implicit val LanguageTextualize : Textualize[Language] =
		Textualize.by (_.externalized[Id] ());
}

