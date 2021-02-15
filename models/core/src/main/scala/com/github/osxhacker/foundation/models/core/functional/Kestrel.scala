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

import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import scalaz.syntax.Ops


/**
 * The '''Kestrel''' type defines a version of the
 * [[https://en.wikipedia.org/wiki/Combinatory_logic#Combinatory_calculi K Combinator]]
 * for use in the system.  What makes this important is that it allows
 * for effectual logic (such as emitting events) to be executed through use of
 * only a [[scalaz.Functor]].  Like any abstraction, '''Kestrel''' ''can be''
 * abused by doing excessive side effects.  Therefore, it is ''highly
 * recommended'' that '''Kestrel''' is used sparingly!
 *
 * @author osxhacker
 */
sealed class Kestrel[M[_]] (implicit F : Functor[M])
{
	/// Class Imports
	import syntax.functor._


	/**
	 * The kestrel method applies ''f''' to an instance of `A`, held within
	 * '''fa''', and discards the result of '''f'''.
	 */
	def kestrel[A] (fa : M[A])
		(f : A => Any)
		: M[A] =
		fa.map {
			a =>
				
			f (a);
			a;
			}
}


object Kestrel
	extends KestrelImplicits
{
	@inline
	def apply[M[_]] () (implicit K : Kestrel[M]) : Kestrel[M] = K;
}


trait KestrelImplicits
{
	/// Implicit Conversions
	implicit def KestrelInstance[M[_] : Functor] : Kestrel[M] = new Kestrel[M];
}


final class KestrelOps[F[_], A] (override val self : F[A])
	(implicit F : Functor[F])
	extends Ops[F[A]]
{
	val K = new Kestrel[F];
	
	def kestrel (f : A => Any) : F[A] = K.kestrel[A] (self) (f);
}


trait ToKestrelOps0
{
	/**
	 * The ToKestrelOpsUnapply `implicit` is ''required to exist'' so that
	 * types which can produce a [[scalaz.Functor]], but are not
	 * mono-parametric, can participate.  A specific use case is the
	 * [[scalaz.\/]] type.
	 */
	implicit def ToKestrelOpsUnapply[FA] (v : FA)
		(implicit F0 : Unapply[Functor, FA])
		: KestrelOps[F0.M, F0.A] =
		new KestrelOps[F0.M, F0.A] (F0 (v)) (F0.TC)
}


trait ToKestrelOps
	extends ToKestrelOps0
{
	/// Implicit Conversions
	implicit def ToKestrelOpsFromFA[F[_] : Functor, A] (fa : F[A]) =
		new KestrelOps[F, A] (fa);
}
