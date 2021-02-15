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

package com.github.osxhacker.foundation.models.core

import scala.collection.immutable
import scala.concurrent.Future

import akka.{
	Done,
	NotUsed
	}

import akka.stream.scaladsl.Source
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}



/**
 * =Overview=
 *
 * The '''akkax''' `package` defines types related to integrating
 * [[https://akka.io/docs/ Akka]] constructs with the idioms provided by the
 * [[com.github.osxhacker.foundation.models.core]] library.
 *
 * @author osxhacker
 *
 */
package object akkax
{
	/// Class Imports


	/// Class Types
	type ImmediateSource[+A] = Source[A, NotUsed]


	object all
		extends ToFlowOps
			with ToSourceOps
			with RichFlowImplicits
			with RichSourceImplicits


	object implicits
		extends RichFlowImplicits
			with RichSourceImplicits


	object streams
		extends ToFlowOps
			with ToSourceOps
			with RichFlowImplicits
			with RichSourceImplicits


	object ImmediateSource
	{
		def apply[A] (iterable : immutable.Iterable[A]) : ImmediateSource[A] =
			Source (iterable);


		def cycle[A] (f : () => Iterator[A]) : ImmediateSource[A] =
			Source.cycle (f);


		def empty[A] : ImmediateSource[A] = Source.empty;


		def failed[A] (cause : Throwable) : ImmediateSource[A] =
			Source.failed (cause);


		def fromFuture[A] (fa : Future[A]) : ImmediateSource[A] =
			Source.fromFuture (fa);


		def fromIterator[A] (f : () => Iterator[A]) : ImmediateSource[A] =
			Source.fromIterator (f);


		def repeat[A] (a : A) : ImmediateSource[A] = Source.repeat (a);


		def single[A] (a : A) : ImmediateSource[A] = Source.single (a);


		def unfold[S, A] (s : S)
			(f : S => Option[(S, A)])
			: ImmediateSource[A] =
			Source.unfold (s) (f);


		def unfoldAsync[S, A] (s : S)
			(f : S => Future[Option[(S, A)]])
			: ImmediateSource[A] =
			Source.unfoldAsync (s) (f);


		def unfoldResource[A, S] (
			create : () => S,
			read : S => Option[A],
			close : S => Unit
			)
			: ImmediateSource[A] =
			Source.unfoldResource (create, read, close);


		def unfoldResourceAsync[A, S] (
			create : () => Future[S],
			read : S => Future[Option[A]],
			close : S => Future[Done]
			)
			: ImmediateSource[A] =
			Source.unfoldResourceAsync (create, read, close);


		def zipN[A] (sources: immutable.Seq[Source[A , _]])
			: ImmediateSource[Seq[A]] =
			Source.zipN (sources);


		def zipWithN[A, O] (zipper : immutable.Seq[A] => O)
			(sources : immutable.Seq[Source[A, _]])
			: ImmediateSource[O] =
			Source.zipWithN (zipper) (sources);
	}
}
