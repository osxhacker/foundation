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

import scala.language.implicitConversions

import shapeless._
import ops.tuple.FlatMapper
import syntax.std.tuple._


sealed trait LowPriorityFlatten
	extends Poly1
{
	implicit def default[T] = at[T] (Tuple1.apply _);
}


object FlattenTuple
	extends LowPriorityFlatten
{
	implicit def caseTuple[A <: Product, HL <: HList] (
		implicit lfm : Lazy[FlatMapper[A, FlattenTuple.type]],
		witness : Lazy[IsTuple[A]]
		)
		= at[A] (lfm.value (_));
}


/**
 * The '''TupleOps''' type defines syntax available for aribtrary `Tuple`s to
 * interact with [[shapless.HList]]s seamlessly.
 *
 * @author osxhacker
 */
final class TuplesOps[P <: Product] (val self : P)
	(implicit witness : IsTuple[P])
{
	/**
	 * The flatten method produces a `Tuple` from `P` in which all "top-level"
	 * `Tuple`s are merged into a single, flat, `Tuple`.  Algebraic data types,
	 * `case class`es, are intentionally excluded from the flattening
	 * process.
	 */
	def flatten (implicit lfm : Lazy[FlatMapper[P, FlattenTuple.type]]) =
		FlattenTuple (self);
}


trait ToTuplesOps
{
	/// Implicit Conversions
	implicit def ToTuplesOps[P <: Product] (p : P)
		(implicit witness : IsTuple[P])
		= new TuplesOps[P] (p);
}

