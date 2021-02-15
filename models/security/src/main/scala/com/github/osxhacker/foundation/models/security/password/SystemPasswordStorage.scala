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
import akka.stream.scaladsl.Source
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.DeploymentEnvironment
import com.github.osxhacker.foundation.models.core.net.URI


/**
 * The '''SystemPasswordStorage''' trait defines the Domain Object Model
 * contract for interacting with an external persistent store responsible for
 * [[com.github.osxhacker.foundation.models.security.password.SystemPassword]] instances.
 *
 * @author osxhacker
 *
 */
trait SystemPasswordStorage
{
	/**
	 * The find method attempts to resolve a
	 * [[com.github.osxhacker.foundation.models.security.password.CleartextPassword]]
	 * instance based on an implementation-defined '''location'''
	 * [[com.github.osxhacker.foundation.models.core.net.URI]].
	 */
	def find (location : URI @@ StorageIndicator)
		(implicit materializer : Materializer)
		: Source[CleartextPassword, NotUsed];


	/**
	 * The query method attempts to resolve all
	 * [[com.github.osxhacker.foundation.models.core.net.URI]] `->`
	 * [[com.github.osxhacker.foundation.models.security.password.CleartextPassword]]
	 * pairs having a common "parent" '''location'''.  Note that an
	 * implementation can produce an empty [[akka.stream.scaladsl.Source]] if
	 * desired.
	 */
	def query (location : URI @@ StorageIndicator)
		(implicit materializer : Materializer)
		: Source[(URI @@ StorageIndicator, CleartextPassword), NotUsed];
}
