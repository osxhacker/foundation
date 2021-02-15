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

import java.util.concurrent.TimeoutException

import scala.concurrent._
import scala.concurrent.duration.Deadline
import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import scala.util.{
	Failure,
	Success
	}

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import scalaz.syntax.Ops


/**
 * The '''FutureImplicits''' type defines Scalaz ''type class''es related to
 * [[scala.concurrent.Future]] instances.
 *
 * @author osxhacker
 *
 */
trait FutureImplicits
{
	private[functional] class FutureMonad (implicit ec : ExecutionContext)
		extends MonadError[Future, Throwable]
			with Zip[Future]
		{
			override def ap[A, B] (fa : => Future[A])
				(fab : => Future[A => B])
				: Future[B] = fab zip fa map {
					case (fa, a) => fa (a);
					}


			override def bind[A, B] (fa : Future[A])
				(f : A => Future[B])
				: Future[B] =
				fa flatMap (f);


			override def handleError[A] (fa : Future[A])
				(f : Throwable => Future[A])
				: Future[A] = fa recoverWith (PartialFunction (f));


			override def map[A, B] (fa : Future[A])
				(f : A => B)
				: Future[B] =
				fa map (f);


			override def point[A] (a : => A) : Future[A] =
				Future (a);


			override def raiseError[A] (e : Throwable) : Future[A] =
				Future.failed (e);


			override def zip[A, B] (fa : => Future[A], fb : => Future[B])
				: Future[(A, B)] =
				fa zip (fb);
		}


	object bifunctor
	{
		/// Class Imports
		import Leibniz.refl


		/**
		 * The unapplyBifunctor `implicit` must be provided so that we can
		 * "forward" the type deconstruction through the `Future[M0[A0, B0]]`
		 * properly.  This is a bit of "type voodoo" and studying Scalaz's
		 * [[scalaz.Unapply2]] is definitely recommended.
		 */
		implicit def unapplyBifunctor[M0[_, _], A0, B0] (
			implicit ec : ExecutionContext,
			B : Bifunctor[M0]
			)
			: Unapply2[Bifunctor, Future[M0[A0, B0]]] {
				type M[X, Y] = Future[M0[X, Y]]
				type A = A0
				type B = B0
				} =
			new Unapply2[Bifunctor, Future[M0[A0, B0]]] {
				type M[X, Y] = Future[M0[X, Y]]
				type A = A0
				type B = B0

				def TC = futureBifunctor[M0];
				def leibniz = refl;
			}


		/**
		 * The `implicit` futureBifunctor method is used both in defining
		 * `unapplyBifunctor` as well as enabling composition of bifunctor
		 * syntax, such as:
		 * 
		 * {{{
		 * 	((_ : Int) + 1) <-: someFuture :-> ((_ : String).reverse);
		 * }}}
		 */
		implicit def futureBifunctor[M[_, _]] (
			implicit ec : ExecutionContext,
			B : Bifunctor[M]
			)
			: Bifunctor[({ type F[X, Y] = Future[M[X, Y]]})#F] =
			new Bifunctor[({ type F[X, Y] = Future[M[X, Y]]})#F] {
				override def bimap[A, B, C, D] (fab : Future[M[A, B]])
					(f : A => C, g : B => D)
					: Future[M[C, D]] =
					fab map (B.bimap (_) (f, g));
				}
	}


	object comonad
	{
		/**
		 * '''Comonad''' support requires not only an
		 * [[scala.concurrent.ExecutionContext]], but also a
		 * [[scala.concurrent.duration.Deadline]], as the `copoint` ability of
		 * taking an ''A'' "out of its context" necessitates the
		 * [[scala.concurrent.duration.Duration]].
		 */
		implicit def futureComonad (
			implicit ec : ExecutionContext,
			t : Deadline
			)
			= new FutureMonad
				with Comonad[Future]
				with Cobind[Future]
				with Traverse[Future]
			{
				/// Class Imports
				import Scalaz._


				override def cobind[A, B] (fa : Future[A])
					(f : Future[A] => B)
					: Future[B] =
					t.hasTimeLeft.fold (
						Future (f (fa)),
						Future.failed (new TimeoutException)
						);


				override def copoint[A] (p : Future[A]) : A =
					t.hasTimeLeft.fold (
						Await.result (p, t.timeLeft),
						throw new TimeoutException ("Deadline expired")
						);


				override def traverseImpl[G[_] : Applicative, A, B] (
					fa : Future[A]
					)
					(f : A => G[B])
					: G[Future[B]] =
					Functor[G].map (f (copoint (fa))) (Future.successful);
			}
	}


	object monad
	{
		implicit def futureMonad (implicit ec : ExecutionContext) =
			new FutureMonad;
	}


	implicit def futureMonoid[T] (implicit ec : ExecutionContext, M : Monoid[T])
		: Monoid[Future[T]] =
		new Monoid[Future[T]] {
			override val zero : Future[T] = Future.successful[T] (M.zero);
			override def append (a : Future[T], b : => Future[T]) : Future[T] =
				for {
					x <- a
					y <- b
					} yield M.append (x, y);
			}
}


trait FutureOps[A]
	extends Ops[Future[A]]
{
	/// Class Imports
	import Scalaz._


	/**
	 * The disjunction method causes any exceptions raised in the `Future[A]`
	 * to be mapped into a `Throwable \/ A`.
	 */
	def disjunction (implicit ec : ExecutionContext) : Future[Throwable \/ A] =
		fold (_.left, _.right);


	/**
	 * The fold method allows a [[scala.concurrent.Future]] to normalize both
	 * [[scala.util.Success]] and [[scala.util.Failure]] results to a common
	 * type `X`.
	 */
	def fold[X] (e : Throwable => X, fa : A => X)
		(implicit ec : ExecutionContext)
		: Future[X] =
	{
		val p = Promise[X] ();

		self onComplete {
			case s : Success[A] =>
				p complete (s map (fa));

			case Failure (error) =>
				p complete (Success (e (error)));
			}

		return (p.future);
	}
}


trait ToFutureOps
{
	/// Implicit Conversions
	implicit class ToFutureOpsFromFuture[A] (override val self : Future[A])
		extends FutureOps[A]
}

