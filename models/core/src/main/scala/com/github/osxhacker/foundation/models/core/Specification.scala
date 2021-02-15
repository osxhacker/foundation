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

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz._


/**
 * The '''Specification''' type is a contract for identifying Domain Object
 * Model types which satisfy the requirements of concrete implementations.
 * They can be viewed as
 * [[http://refactoring.com/catalog/replaceMethodWithMethodObject.html Method Objects]]
 * suitable for use in `filter`ing candidate instances.
 *
 * @author osxhacker
 *
 */
trait Specification[T]
	extends (T => Boolean)
		with Serializable
{
	/// Class Imports
	import syntax.isEmpty._
	import syntax.monadPlus._


	/// Instance Properties
	override def toString : String = "<specification>";


	/**
	 * The isSatisfiedBy method is a named alias for the `apply` method.
	 */
	final def isSatisfiedBy (candidate : T) : Boolean = apply (candidate);


	/**
	 * The whenGiven method provides an easy way to minimize having to duplicate
	 * '''Specification''' satisfaction checks of the form:
	 * 
	 * {{{
	 * (ma.isEmpty || ma.exists (_ === x));
	 * }}}
	 */
	protected def whenGiven[M[_], A] (ma : M[A])
		(f : A => Boolean)
		(implicit IE : IsEmpty[M], MP : MonadPlus[M])
		: Boolean =
		IE.isEmpty (ma) || !IE.isEmpty (MP.filter (ma) (f));
}


final case class AndSpecification[T] (
	private val lhs : Specification[T],
	private val rhs : Specification[T]
	)
	extends Specification[T]
{
	/// Instance Properties
	override lazy val toString : String = "(%s) && (%s)".format (lhs, rhs);


	override def apply (candidate : T) : Boolean =
		lhs (candidate) && rhs (candidate);
}


final case class NotSpecification[T] (private val spec : Specification[T])
	extends Specification[T]
{
	/// Instance Properties
	override lazy val toString : String = "!(%s)".format (spec);


	override def apply (candidate : T) : Boolean = !spec (candidate);
}


final case class OrSpecification[T] (
	private val lhs : Specification[T],
	private val rhs : Specification[T]
	)
	extends Specification[T]
{
	/// Instance Properties
	override lazy val toString : String = "(%s) || (%s)".format (lhs, rhs);


	override def apply (candidate : T) : Boolean =
		lhs (candidate) || rhs (candidate);
}


object Specification
{
	/**
	 * The apply method allows for creation of a '''Specification''' using an
	 * anonymous '''inline''' method.  Care must be taken when using this as
	 * transmitting a '''Specification''' made this way over a network will
	 * likely fail.
	 */
	def apply[T] (inline : T => Boolean) : Specification[T] =
		new Specification[T] {
			override def apply (candidate : T) : Boolean =
				inline (candidate);
			}


	/// Implicit Conversions
	implicit class ComposeSpecification[A] (val lhs : Specification[A])
		extends AnyVal
	{
		/**
		 * The && method composes two instances of '''Specification''',
		 * with this instance first deciding satisfaction ''and then'' the rhs.
		 */
		def && (rhs : Specification[A]) : Specification[A] =
			AndSpecification (lhs, rhs);


		/**
		 * The || method composes two instances of '''Specification''', with
		 * this instance first deciding satisfaction ''or else'' the rhs.
		 */
		def || (rhs : Specification[A]) : Specification[A] =
			OrSpecification (lhs, rhs);


		/**
		 * The unary_! method allows logical negation of a '''Specification'''.
		 */
		def unary_! : Specification[A] = NotSpecification (lhs);
	}
}

