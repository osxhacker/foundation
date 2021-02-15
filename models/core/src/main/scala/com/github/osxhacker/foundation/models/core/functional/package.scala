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

import scala.concurrent.{
	ExecutionContext,
	Future
	}

import scala.util.{
	Failure,
	Success,
	Try
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''functional''' `package` provides functional-style support for types
 * such as [[scala.concurrent.Future]] so that they can participate in
 * Scalaz-style collaborations.
 *
 * @author osxhacker
 *
 */
package object functional
{
	/// Class Types
	type ErrorOr[+A] = ApplicationError \/ A

	type Tristate[A] = TristateT[Option, A]


	object all
		extends ContainerFunctorImplicits
			with FilterableImplicits
			with FutureImplicits
			with HeadOptionImplicits
			with KestrelImplicits
			with ToHeadOptionOps
			with ToFutureOps
			with ToKestrelOps
			with ToTuplesOps
			with ToContainerFunctorOps
			with ToCodistributiveOps


	object containers
		extends ContainerFunctorImplicits
			with ToContainerFunctorOps
			with ToCodistributiveOps


	object filterable
		extends FilterableImplicits


	object futures
		extends FutureImplicits
			with ToFutureOps
	{
		/// Class Types
		type FutureEither[T] = EitherT[Future, ApplicationError, T]


		object FutureEither
		{
			/// Class Imports
			import futures.monad._
			import syntax.bifunctor._
			import syntax.std.`try`._


			def apply[A] (a : ApplicationError \/ A)
				(implicit EC : ExecutionContext)
				: FutureEither[A] =
				EitherT (Future (a));


			def fromTry[A] (ta : Try[A])
				(wrapper : Throwable => ApplicationError)
				(implicit EC : ExecutionContext)
				: FutureEither[A] =
				apply (wrapper <-: ta.toDisjunction);


			/// Implicit Conversions
			implicit def ToApplicative (implicit EC : ExecutionContext)
				: Applicative[FutureEither] =
				new Applicative[FutureEither] {
					override def ap[A, B] (a : => FutureEither[A])
						(f : => FutureEither[A => B])
						: FutureEither[B] =
						a.flatMap (x => f.map (_ (x)));


					override def point[A] (a : => A) : FutureEither[A] =
						EitherT.rightT (Future.successful (a));
					}
		}


		/// Implicit Conversions
		implicit class MapFutureEither[A] (val self : FutureEither[A])
			extends AnyVal
		{
			/**
			 * The flatMapE method applies '''f''' to the `\/` within the
			 * [[com.github.osxhacker.foundation.models.core.functional.FutureEither]] in
			 * the most "parallel friendly" manner possible.
			 */
			def flatMapE[B] (f : A => ApplicationError \/ B)
				(implicit EC : ExecutionContext)
				: FutureEither[B] =
				self.mapT (_.map (_.flatMap (f)));


			/**
			 * The toFutureTry method maps a
			 * [[com.github.osxhacker.foundation.models.core.functional.FutureEither]]
			 * into a ''Future[Try[A]]'' instance.
			 */
			def toFutureTry ()
				(implicit EC : ExecutionContext)
				: Future[Try[A]] =
				self.run.map (std.`try`.fromDisjunction);


			/**
			 * The valueOrFail method `transform`s a
			 * [[com.github.osxhacker.foundation.models.core.functional.FutureEither]]
			 * into a ''Future[A]'' instance, with ''-\/'' instances becoming a
			 * ''Failure''.
			 */
			def valueOrFail ()
				(implicit EC : ExecutionContext)
				: Future[A] =
				self.run.transform {
					case Success (\/- (a)) =>
						Success (a);

					case Success (-\/ (e)) =>
						Failure (e);

					case Failure (e) =>
						Failure (e);
					}
		}


		implicit class ToFutureEither[T] (val self : ApplicationError \/ T)
			extends AnyVal
		{
			def toFutureEither () : FutureEither[T] =
				EitherT.eitherT (Future.successful (self));
		}


		implicit class ToFutureEitherFromFuture[T] (
			val self : Future[ApplicationError \/ T]
			)
			extends AnyVal
		{
			def toFutureEither () : FutureEither[T] = EitherT.eitherT (self);
		}
	}


	object headOption
		extends HeadOptionImplicits
			with ToHeadOptionOps


	object kestrel
		extends ToKestrelOps
			with KestrelImplicits


	object tristate
		extends TristateFunctions
			with TristateTInstances


	object tristatet
		extends TristateTFunctions
			with TristateTInstances


	object tuples
		extends ToTuplesOps
}

