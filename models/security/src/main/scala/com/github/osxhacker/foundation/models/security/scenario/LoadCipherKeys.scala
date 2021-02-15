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

import akka.stream.Materializer
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	DeploymentEnvironment,
	Scenario
	}

import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.security.encryption.{
	CipherKeys,
	KeyStorage
	}


/**
 * The '''LoadCipherKeys''' type defines the
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for
 * hydrating [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]]
 * instances by way of the provided
 * [[com.github.osxhacker.foundation.models.security.encryption.KeyStorage]].
 *
 * @author osxhacker
 *
 */
final case class LoadCipherKeys (
	private val deployment : DeploymentEnvironment
	)
	(
		implicit private val storage : KeyStorage,
		private val m : Materializer
	)
	extends Scenario[FutureEither[Vector[CipherKeys]]]
{
	/// Class Imports
	import akkax.streams._


	override def apply () : FutureEither[Vector[CipherKeys]] =
		storage.load (deployment)
			.toFutureEitherCollection[Vector];
}

