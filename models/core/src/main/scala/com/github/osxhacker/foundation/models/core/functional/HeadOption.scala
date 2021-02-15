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

import scala.collection._
import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import scalaz._


/**
 * The '''HeadOption''' type is a model of the TYPE CLASS pattern and abstracts
 * the ability for an arbitrary container `M[_]` having a `headOption` method
 * (or equivalent, if not specifically named as such) to produce a container
 * having either 0 or 1 instances.
 *
 * @author osxhacker
 *
 */
trait HeadOption[M[_], A]
{
	def headOption[N[_]] (ma : M[A])
		(implicit A : Applicative[N], M : Monoid[N[A]])
		: N[A];
}


object HeadOption
	extends HeadOptionImplicits
{
	def apply[M[_], A] (implicit HO : HeadOption[M, A]) = HO;
}


trait HeadOptionImplicits
{
	/// Class Imports
	import Scalaz._


	/// Class Types
	implicit def nelHeadOption[A]
		: HeadOption[NonEmptyList, A] =
		new HeadOption[NonEmptyList, A] {
			override def headOption[N[_]] (ma : NonEmptyList[A])
				(implicit A : Applicative[N], M : Monoid[N[A]])
				: N[A] =
				ma.head.point[N];
			}


	implicit def optionHeadOption[A]
		: HeadOption[Option, A] =
		new HeadOption[Option, A] {
			override def headOption[N[_]] (ma : Option[A])
				(implicit A : Applicative[N], M : Monoid[N[A]])
				: N[A] =
				ma.fold[N[A]] (M.zero) (_.point[N]);
			}


	implicit def traversableHeadOption[M[X] <: Traversable[X], A]
		: HeadOption[M, A] =
		new HeadOption[M, A] {
			override def headOption[N[_]] (ma : M[A])
				(implicit A : Applicative[N], M : Monoid[N[A]])
				: N[A] =
				ma.headOption.fold[N[A]] (M.zero) (_.point[N]);
			}
}


trait HeadOptionOps[M[_], A]
{
	implicit def HO : HeadOption[M, A];
	def self : M[A];


	def headOption[N[_]] (implicit A : Applicative[N], M : Monoid[N[A]])
		: N[A] =
		HO.headOption[N] (self)
}


trait ToHeadOptionOps
{
	implicit def headOptionOps[M[_], A] (ma : M[A])
		(implicit F : HeadOption[M, A])
		: HeadOptionOps[M, A] =
		new HeadOptionOps[M, A] {
			override val self = ma;
			override val HO = F;
			}
}

