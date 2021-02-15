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

import akka.util.ByteString
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.StringValidations
import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''CleartextPassword''' type defines the Domain Object Model concept of
 * a [[com.github.osxhacker.foundation.models.security.password.Password]] __candidate__
 * which has been provided by an external actor.  By definition, it is neither
 * secure nor to be considered validated.
 *
 * @author osxhacker
 *
 */
final case class CleartextPassword private (val content : String)
{
	override def toString () : String =
		"CleartextPassword(%s)".format ("X" * content.length);


	/**
	 * The toByteString method creates a [[akka.util.ByteString]] having the
	 * relevant `Byte`s needed to incorporate into a secure hash operation.
	 */
	def toByteString () : ByteString = ByteString (content.getBytes);
}


object CleartextPassword
	extends StringValidations
{
	/**
	 * The apply method validates the '''clear''' text `String` to ensure a
	 * valid '''CleartextPassword''' instance can be created and, if so,
	 * creates one.
	 */
	def apply (clear : String) : ApplicationError \/ CleartextPassword =
		fromString[CleartextPassword] {
			implicit v =>

			(notEmpty () >==> trim ()).run (clear)
				.map (new CleartextPassword (_));
			}


	/// Implicit Conversions
	implicit val CleartextPasswordEqual : Equal[CleartextPassword] =
		Equal.equalA;
}

