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

import java.lang.Character.{
	isISOControl,
	isWhitespace
	}

import scala.util.parsing.combinator._

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	ParsingError
	}


/**
 * The '''EmailAddressParser''' trait defines a
 * [[https://www.artima.com/pins1ed/combinator-parsing.html Parser Combinator]]
 * for parsing
 * [[https://tools.ietf.org/html/rfc5322#section-3.4 IETF Address Specification]]
 * `String`s.  For descriptions of the terms used here, such as `atext` and
 * `addrSpec`, refer to the IETF specification.
 *
 * @author osxhacker
 *
 */
trait EmailAddressParser
	extends RegexParsers
{
	/// Class Imports
	import syntax.either._
	import syntax.std.boolean._


	/**
	 * The parseEmailAddresss method will consume '''all''' of the characters
	 * in '''candidate''', producing a `(local, domain)` pair if successful.
	 */
	final def parseEmailAddresss (candidate : String)
		: ApplicationError \/ (String, String) =
		parseAll (addrSpec, candidate.trim ()) match {
			case error : NoSuccess =>
				ParsingError (error.msg).left;

			case Success (pair, next) =>
				next.atEnd.fold (
					pair.right,
					ParsingError ("invalid address format").left
					);
			}


	private def addrSpec : Parser[(String, String)] =
		localPart ~ domain ^^ {
			case local ~ d =>

			(local, d);
			}


	private def atext : Parser[Char] =
		elem ("atext", ch =>
			!(
				/// 'bang paths' and relays are disallowed
				"""!%[]()<>:;@\,.""".contains (ch) ||
				isISOControl (ch) ||
				isWhitespace (ch)
			));


	private def domain : Parser[String] =
		("@" ~> (dotAtom | domainLiteral)) | failure ("missing domain");


	private def domainLiteral : Parser[String] =
		"[" ~> rep (dtext) <~ "]" ^^ {
			_.mkString;
			}


	private def dot : Parser[Char] = elem ('.');


	private def dotAtom : Parser[String] =
		atext ~ rep (dot | atext) ^^ {
			case head ~ tail =>

			tail.foldLeft (new StringBuilder ().append (head)) {
				_.append (_)
				}.toString;
			}


	private def dtext : Parser[Char] =
		elem ("dtext", ch =>
			!(
				isISOControl (ch) ||
				isWhitespace (ch) ||
				"""[]\""".contains (ch)
			));


	private def localPart : Parser[String] =
		quotedString | dotAtom | failure ("missing local-part");


	private def qtext : Parser[Char] =
		elem ("qtext", ch => ch != '\\' && ch != '"');


	private def quotedString : Parser[String] =
		elem ('"') ~> rep (qtext) <~ elem ('"') ^^ {
			_.mkString;
			}
}


object EmailAddressParser
	extends EmailAddressParser
{
	def apply (candidate : String) : ApplicationError \/ (String, String) =
		parseEmailAddresss (candidate);
}
