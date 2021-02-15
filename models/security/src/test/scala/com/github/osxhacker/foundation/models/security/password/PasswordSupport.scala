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

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.DeploymentEnvironment
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	ConfigurationError
	}

import com.github.osxhacker.foundation.models.core.net
import com.github.osxhacker.foundation.models.security.scenario.CreatePassword


/**
 * The '''PasswordSupport''' trait defines supporting types and behaviour for
 * testing `com.github.osxhacker.foundation.models.security.password`-related logic.
 *
 * @author osxhacker
 *
 */
trait PasswordSupport
{
	/// Class Imports
	import net.URI
	import syntax.std.option._


	/// Class Types
	/**
	 * The '''MockSystemPasswordStorage''' type defines a lightweight
	 * implementation of the
	 * [[com.github.osxhacker.foundation.models.security.password.SystemPasswordStorage]]
	 * contract.
	 *
	 * @author osxhacker
	 *
	 */
	final case class MockSystemPasswordStorage (
		val passwords : Map[URI @@ StorageIndicator, CleartextPassword]
		)
		extends SystemPasswordStorage
	{
		override def find (physical : URI @@ StorageIndicator)
			(implicit materializer : Materializer)
			: Source[CleartextPassword, NotUsed] =
			passwords.get (physical) cata (
				Source.single (_),
				Source.failed (
					ConfigurationError (s"unknown password uri=${physical}")
					)
				);


		override def query (location : URI @@ StorageIndicator)
			(implicit materializer : Materializer)
			: Source[(URI @@ StorageIndicator, CleartextPassword), NotUsed] =
			Source.fromIterator (() => passwords.iterator);
	}


	object MockSystemPasswordStorage
	{
		/// Class Imports
		import std.list._
		import syntax.all._


		def apply (
			candidates : List[(URI, ApplicationError \/ CleartextPassword)]
			)
			: MockSystemPasswordStorage =
			new MockSystemPasswordStorage (
				candidates.traverseU {
					case (uri, clear) =>
						clear.map (StorageIndicator (uri) -> _);
					}
					.map (_.toMap)
					.valueOr (e => throw e)
				);
	}


	protected def passwordFor (candidate : String)
		: ApplicationError \/ OneWayPassword =
		CleartextPassword (candidate).map (CreatePassword (_) ());
}
