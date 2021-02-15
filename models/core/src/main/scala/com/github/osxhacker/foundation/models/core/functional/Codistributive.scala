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
	Sink => _,
	Source => _,
	Success => _,
	_
	}


/**
 * The '''Codistributive''' type defines a generic form of `join` which
 * specifically allows for ''F[G[A]]'' containers to produce an ''F[A]''.
 * It is the complement of the [[scalaz.Distributive]] type class.
 *
 * @author osxhacker
 *
 * @see [[com.github.osxhacker.foundation.models.core.functional.ContainerFunctor]]
 */
trait Codistributive[F[_], G[_]]
{
	def coexpand[A] (fga : F[G[A]])
		(cf : ContainerFunctor[G, F, A])
		: F[A];
}


object Codistributive
	extends CodistributiveImplicits
{
	@inline
	def apply[F[_], G[_]] ()
		(implicit CD : Codistributive[F, G])
		: Codistributive[F, G] =
		CD;
}


sealed trait CodistributiveImplicitsLow
{
	/// Implicit Conversions
	implicit def codistributive[F[_], G[_]] (implicit B : Bind[F])
		: Codistributive[F, G] =
		new Codistributive[F, G] {
			def coexpand[A] (fga : F[G[A]])
				(cf : ContainerFunctor[G, F, A])
				: F[A] =
				B.bind (fga) (cf.transform);
			}
}


sealed trait CodistributiveImplicits
	extends CodistributiveImplicitsLow
{
	/// Implicit Conversions
	/**
	 * Sets are a special case in that `scalaz` does not define a
	 * [[scalaz.Bind]] type class for them.
	 */
	implicit def codistributiveSet[G[_]]
		: Codistributive[Set, G] =
		new Codistributive[Set, G] {
			def coexpand[A] (fga : Set[G[A]])
				(cf : ContainerFunctor[G, Set, A])
				: Set[A] =
				fga.flatMap (cf.transform);
			}
}


sealed class CodistributiveOps[F[_], G[_], A] (
	override val self : F[G[A]]
	)
	(implicit CD : Codistributive[F, G], CF : ContainerFunctor[G, F, A])
	extends syntax.Ops[F[G[A]]]
{
	def coexpand () : F[A] = CD.coexpand[A] (self) (CF.transform);
}


trait ToCodistributiveOps
{
	/// Implicit Conversions
	implicit def codistributiveOps[F[_], G[_], A] (fga : F[G[A]])
		(implicit CD : Codistributive[F, G], CF : ContainerFunctor[G, F, A])
		: CodistributiveOps[F, G, A] =
		new CodistributiveOps[F, G, A] (fga);
}
