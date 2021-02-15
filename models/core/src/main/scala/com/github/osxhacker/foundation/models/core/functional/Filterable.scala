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

import scala.language.higherKinds

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}


/**
 * The '''Filterable''' trait is a model of the TYPE CLASS pattern and
 * abstracts the ability of an arbitrary container ''F'' having either or both
 * the `filter` and `filterNot` methods.  Default implementations for
 * `mutable`, `immutable`, and `generic` Scala collections are defined here.
 * Types which are __not__ in the Scala SDK can provided their own
 * '''Filterable''' definitions as applicable.
 *
 * Since [[com.github.osxhacker.foundation.models.core.Specification]] is a model of
 * `A =&gt; Boolean`, all [[com.github.osxhacker.foundation.models.core.Specification]]s
 * are suitable predicates for `filter` and `filterNot`.
 *
 * @author osxhacker
 *
 */
trait Filterable[F[_]]
{
	/**
	 * The filter method must apply '''p''' to each element of '''fa''' and
	 * produce a new `M[A]` where all ''A''s which satisfy '''p''' are
	 * present in the result.
	 */
	def filter[A] (fa : F[A])
		(p : A => Boolean)
		: F[A];


	/**
	 * The filterNot method must apply '''p''' to each element of '''fa''' and
	 * produce a new `M[A]` where all ''A''s which __do not__ satisfy '''p'''
	 * are present in the result.  The default version delegates to the
	 * `filter` method by creating a functor which negates '''p'''.
	 */
	def filterNot[A] (fa : F[A])
		(p : A => Boolean)
		: F[A] =
		filter (fa) (a => !p (a));
}


object Filterable
	extends FilterableImplicits
{
	/**
	 * The apply method is provided to support "summoning" a
	 * `Filterable[M]`.
	 */
	def apply[M[_]] (implicit F : Filterable[M]) = F;
}


/**
 * The '''FilterableImplicits''' trait defines the "built-in"
 * [[com.github.osxhacker.foundation.models.core.functional.Filterable]] instances
 * supported.  This includes:
 *
 *   - [[scala.collection]]
 *
 *   - [[scala.collection.mutable]]
 *
 *   - [[scala.collection.immutable]]
 *
 * @author osxhacker
 *
 */
trait FilterableImplicits
	extends FilterableImplicits0
{
	/// Implicit Conversions
	implicit val OptionFilterable : Filterable[Option] =
		new Filterable[Option] {
			override def filter[A] (fa : Option[A])
				(p : A => Boolean)
				: Option[A] =
				fa filter (p);


			override def filterNot[A] (fa : Option[A])
				(p : A => Boolean)
				: Option[A] =
				fa filterNot (p);
			}
}


sealed trait FilterableImplicits0
{
	/// Class Imports
	import scala.collection.TraversableLike


	/// Implicit Conversions
	implicit def TraversableLikeFilterable[T, M[T] <: TraversableLike[T, M[T]]]
		: Filterable[M] =
		new Filterable[M] {
			override def filter[A] (fa : M[A])
				(p : A => Boolean)
				: M[A] =
				fa filter (p);


			override def filterNot[A] (fa : M[A])
				(p : A => Boolean)
				: M[A] =
				fa filterNot (p);
			}
}

