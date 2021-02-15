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

package com.github.osxhacker.foundation.models.security.encryption

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
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.core.net.Scheme


/**
 * The '''KeyStorage''' trait defines the contract for managing
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] such that
 * they are able to be resolved based on the
 * [[com.github.osxhacker.foundation.models.core.DeploymentEnvironment]].  Each produced
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] will have
 * distinct [[com.github.osxhacker.foundation.models.security.encryption.KeyVersion]]s
 * associated with them.
 *
 * @author osxhacker
 *
 */
trait KeyStorage
{
	/**
	 * This version of the load method attempts to resolve all
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] instances
	 * based on an implementation-defined
	 * [[com.github.osxhacker.foundation.models.core.DeploymentEnvironment]].  There can
	 * be multiple due to the
	 * [[com.github.osxhacker.foundation.models.security.encryption.KeyVersion]]
	 * associated with each individual
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]].
	 */
	def load ()
		(implicit materializer : Materializer)
		: Source[CipherKeys, NotUsed];


	/**
	 * This version of the load method attempts to resolve all
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] instances
	 * based on the desired '''deployment'''.  There can be multiple due to
	 * the [[com.github.osxhacker.foundation.models.security.encryption.KeyVersion]]
	 * associated with each individual
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]].
	 */
	def load (deployment : DeploymentEnvironment)
		(implicit materializer : Materializer)
		: Source[CipherKeys, NotUsed];


	/**
	 * This version of the generate method produces a new
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] such that
	 * it can be resolved via `load` at some point in the future.  The
	 * [[com.github.osxhacker.foundation.models.core.DeploymentEnvironment]] used will be
	 * chosen by the implementation.
	 * 
	 * The returned [[akka.stream.Source]] will contain all known
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] instances,
	 * which includes the newly minted one as well.
	 */
	def generate ()
		(implicit materializer : Materializer)
		: Source[CipherKeys, NotUsed];


	/**
	 * This version of the generate method produces a new
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] such that
	 * it can be resolved via `load` at some point in the future.  The returned
	 * [[akka.stream.Source]] will contain all known
	 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] instances,
	 * which includes the newly minted one as well.
	 */
	def generate (deployment : DeploymentEnvironment)
		(implicit materializer : Materializer)
		: Source[CipherKeys, NotUsed];
}

