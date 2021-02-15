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

package com.github.osxhacker.foundation.models.core.functional

import scala.concurrent.Future
import scala.language.higherKinds

import akka.NotUsed
import akka.stream.scaladsl.Source
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.ImmediateSource
import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''UnwrapMA''' generic type provides the ability to deconstruct, or
 * "unwrap", supported ''MA'' types into their constituent ''M[_]'' and ''A''
 * types.
 *
 * @author osxhacker
 *
 */
trait UnwrapMA[MA, M[_], A]
	extends Serializable
{
	/// Class Types


	/// Instance Properties
	val leibniz : Leibniz.===[MA, M[A]]


	def apply (ma : MA) : M[A] = leibniz (ma);
}


object UnwrapMA
{
	/// Class Impports
	import futures.FutureEither


	/// Implicit Conversions
	implicit def eitherUnwrapMA[A] (
		implicit l : Leibniz.===[ApplicationError \/ A, ErrorOr[A]]
		)
		: UnwrapMA[ApplicationError \/ A, ErrorOr, A] =
		new UnwrapMA[ApplicationError \/ A, ErrorOr, A] {
			override val leibniz = l;
			}


	implicit def sourceUnwrapMA[A] (
		implicit l : Leibniz.===[Source[A, NotUsed], ImmediateSource[A]]
		)
		: UnwrapMA[Source[A, NotUsed], ImmediateSource, A] =
		new UnwrapMA[Source[A, NotUsed], ImmediateSource, A] {
			override val leibniz = l;
			}


	implicit def faUnwrapMA[F[_], A] (implicit l : Leibniz.===[F[A], F[A]])
		: UnwrapMA[F[A], F, A] =
		new UnwrapMA[F[A], F, A] {
			override val leibniz = l;
			}
}
