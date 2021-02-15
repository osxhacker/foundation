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

import scala.collection.GenTraversableOnce
import scala.collection.{
	immutable,
	mutable
	}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContext
import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.akkax.ImmediateSource

import futures.FutureEither


/**
 * The '''ContainerFunctor''' trait defines an
 * [[https://en.wikipedia.org/wiki/Epimorphism Epimorphism]] where the domain
 * is the set of parameterized types `F[_]`  and the codomain is the set of
 * resultant parameterized types `G[_]`.
 *
 * @author osxhacker
 *
 * @see [[com.github.osxhacker.foundation.models.core.functional.ContainerFunctor.ContainerFunctorLaw]]
 */
trait ContainerFunctor[F[_], G[_], A]
{
	/// Self Type Constraits
	self =>


	/// Class Types
	trait ContainerFunctorLaw
	{
		/// Class Imports
		import syntax.equal._


		def empty ()
			(implicit MF : Monoid[F[A]], IE : IsEmpty[G])
			: Boolean =
			IE.isEmpty (transform (MF.zero));


		def nonempty (a : A)
			(implicit A : Applicative[F], IE : IsEmpty[G])
			: Boolean =
			!IE.isEmpty (transform (A.point (a)));
	}


	/// Instance Properties
	def containerFunctorLaw = new ContainerFunctorLaw {
		}


	final def andThen[H[_]] (next : ContainerFunctor[G, H, A])
		: ContainerFunctor[F, H, A] =
		new ContainerFunctor[F, H, A] {
			override def transform (fa : F[A]) : H[A] =
				next.transform (self.transform (fa));
			}


	final def compose[E[_]] (inner : ContainerFunctor[E, F, A])
		: ContainerFunctor[E, G, A] =
		new ContainerFunctor[E, G, A] {
			override def transform (fe : E[A]) : G[A] =
				self.transform (inner.transform (fe));
			}


	/**
	 * The transform method establishes the mapping from `F[_]` to `G[_]` for
	 * all ''A''.
	 */
	def transform (fa : F[A]) : G[A];
}


object ContainerFunctor
	extends ContainerFunctorImplicits
{
	@inline
	def apply[F[_], G[_], A] ()
		(implicit cf : ContainerFunctor[F, G, A])
		: ContainerFunctor[F, G, A] =
		cf;
}


sealed trait ContainerFunctorImplicitsCollectionsLow
{
	/// Implicit Conversions
	implicit def genTraversableToCollection[
		S[X] <: GenTraversableOnce[X],
		A,
		C[_]
		] (implicit cbf : CanBuildFrom[Nothing, A, C[A]])
		: ContainerFunctor[S, C, A] =
		new ContainerFunctor[S, C, A] {
			override def transform (fa : S[A]) : C[A] = fa.to[C];
			}
}


sealed trait ContainerFunctorImplicitsCollections
	extends ContainerFunctorImplicitsCollectionsLow
{
	/// Implicit Conversions
	implicit def collectionToBuffer[S[X] <: GenTraversableOnce[X], A]
		: ContainerFunctor[S, mutable.Buffer, A] =
		new ContainerFunctor[S, mutable.Buffer, A] {
			override def transform (fa : S[A]) : mutable.Buffer[A] =
				fa.toBuffer;
			}


	implicit def collectionToList[S[X] <: GenTraversableOnce[X], A]
		: ContainerFunctor[S, List, A] =
		new ContainerFunctor[S, List, A] {
			override def transform (fa : S[A]) : List[A] = fa.toList;
			}


	implicit def collectionToSource[S[X] <: immutable.Iterable[X], A]
		: ContainerFunctor[S, ImmediateSource, A] =
		new ContainerFunctor[S, ImmediateSource, A] {
			override def transform (fa : S[A]) : ImmediateSource[A] =
				ImmediateSource (fa);
			}


	implicit def errorOrToFutureEither[A] (
		implicit ec : ExecutionContext
		)
		: ContainerFunctor[ErrorOr, FutureEither, A] =
		new ContainerFunctor[ErrorOr, FutureEither, A] {
			override def transform (ea : ErrorOr[A]) : FutureEither[A] =
				FutureEither (ea);
			}


	implicit def errorOrToSource[A]
		: ContainerFunctor[ErrorOr, ImmediateSource, A] =
		new ContainerFunctor[ErrorOr, ImmediateSource, A] {
			override def transform (ea : ErrorOr[A]) : ImmediateSource[A] =
				ea.fold (
					ImmediateSource.failed _,
					ImmediateSource.single _
					);
			}


	implicit def optionToCollection[A, C[_]] (
		implicit cbf : CanBuildFrom[Nothing, A, C[A]]
		)
		: ContainerFunctor[Option, C, A] =
		new ContainerFunctor[Option, C, A] {
			override def transform (fa : Option[A]) : C[A] = {
				val buffer = cbf ();

				fa foreach (buffer += _);

				buffer.result ();
				}
			}
}


trait ContainerFunctorImplicits
	extends ContainerFunctorImplicitsCollections
{
	/// Implicit Conversions
	implicit def identityContainer[F[_], A] : ContainerFunctor[F, F, A] =
		new ContainerFunctor[F, F, A] {
			override def transform (fa : F[A]) : F[A] = fa;
			}
}


final class ContainerFunctorOps[F[_], A] private[functional] (
	override val self : F[A]
	)
	extends syntax.Ops[F[A]]
{
	def transform[G[_]] ()
		(implicit CF: ContainerFunctor[F, G, A])
		: G[A] = CF.transform (self);
}


sealed trait ToContainerFunctorOpsLow
{
	implicit def containerFunctorOpsU[FA, F[_], A] (fa : FA)
		(implicit U : UnwrapMA[FA, F, A])
		: ContainerFunctorOps[F, A] =
		new ContainerFunctorOps[F, A] (U (fa));
	
}


trait ToContainerFunctorOps
	extends ToContainerFunctorOpsLow
{
	/// Implicit Conversions
	implicit def containerFunctorOps[F[_], A] (fa : F[A])
		: ContainerFunctorOps[F, A] =
		new ContainerFunctorOps[F, A] (fa);
}
