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

package com.github.osxhacker.foundation.models.security.scenario

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

import com.github.osxhacker.foundation.models.core.{
	Identifier,
	Scenario
	}

import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.security.encryption._


/**
 * The '''GenerateCipherKeys''' type defines the
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for
 * producing [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]]
 * instances by way of the `implicit`
 * [[com.github.osxhacker.foundation.models.security.encryption.RandomKeyGenerator]]
 * defined for the desired ''AlgoT''.
 *
 * @author osxhacker
 *
 */
final case class GenerateCipherKeys[AlgoT <: Algorithm[AlgoT]] (
	private val version : KeyVersion
	)
	(implicit generator : RandomKeyGenerator[AlgoT])
	extends Scenario[CipherKeys]
{
	override def apply () : CipherKeys = generator (version);
}


object GenerateCipherKeys
{
	def apply[AlgoT <: Algorithm[AlgoT]] (existing : Traversable[CipherKeys])
		(implicit generator : RandomKeyGenerator[AlgoT])
		: GenerateCipherKeys[AlgoT] =
		apply[AlgoT] (existing.maxBy (_.version));


	def apply[AlgoT <: Algorithm[AlgoT]] (existing : CipherKeys)
		(implicit generator : RandomKeyGenerator[AlgoT])
		: GenerateCipherKeys[AlgoT] =
		new GenerateCipherKeys[AlgoT] (existing.version.next ());
}

