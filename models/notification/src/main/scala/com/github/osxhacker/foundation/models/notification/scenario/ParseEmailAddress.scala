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

package com.github.osxhacker.foundation.models.notification.scenario

import akka.NotUsed
import akka.stream.scaladsl.Source
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.text.encoding.Base64
import com.github.osxhacker.foundation.models.notification.{
	EmailAddress,
	EmailAddressParser
	}

import com.github.osxhacker.foundation.models.security.hash.MD5


/**
 * The '''ParseEmailAddress''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for parsing
 * a '''candidate'''
 * [[com.github.osxhacker.foundation.models.notification.EmailAddress]].
 *
 * @author osxhacker
 *
 */
final case class ParseEmailAddress (
	private val candidate : String
	)
	extends Scenario[ApplicationError \/ EmailAddress]
		with EmailAddressParser
{
	override def apply () : ApplicationError \/ EmailAddress =
		parseEmailAddresss (candidate) map ((createAddress _).tupled);
	
	
	/**
	 * The source method produces a [[akka.stream.scaladsl.Source]] based on
	 * the results of `apply`, failing when it `isLeft`.
	 */
	def source : Source[EmailAddress, NotUsed] =
		apply.fold (Source.failed, Source.single);
	
	
	private def createAddress (local : String, domain : String)
		: EmailAddress =
		EmailAddress[MD5, Base64] (local, domain);
}

