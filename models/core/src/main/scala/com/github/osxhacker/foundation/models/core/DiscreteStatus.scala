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
import scala.reflect.ClassTag

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''DiscreteStatus''' type defines the ability to create types which
 * provide a distinct status indicator, such as "Active" or "Suspended", to
 * Domain Object Model [[com.github.osxhacker.foundation.models.core.Entity]]
 * abstractions.
 *
 * @author osxhacker
 */
abstract class DiscreteStatus[T <: DiscreteStatus[T] : ClassTag]
	extends Equals
		with Serializable
{
	/// Self Type Constraints
	this : T =>
		
		
	/// Class Imports
	import syntax.std.boolean._
	
	
	/// Instance Properties
	def name : String;

	
	override def canEqual (that : Any) : Boolean = that.asInstanceOf[T] ne null;


	override def equals (that : Any) : Boolean =
		canEqual (that) && name.equalsIgnoreCase (
			that.asInstanceOf[T].name
			);
	
	
	override def hashCode () : Int = name.hashCode ();
	
	
	override def toString = "%s(%s)".format (
		implicitly[ClassTag[T]].runtimeClass.getName,
		name
		);
	
	
	/**
	 * The liftMap method allows an execution path to be chosen based on whether
	 * or not '''this''' and the '''other''' instances represent the same
	 * status.  If so, the value produced by '''same''' is used.  Otherwise,
	 * '''different''' is chosen to produce an `X` instance.
	 */
	def liftMap[M[_, _], X, Y] (other : DiscreteStatus[T])
		(same : => M[X, Y], different : => M[X, Y])
		(implicit E : Equal[DiscreteStatus[T]])
		: M[X, Y] = E.equal (this, other) fold (same, different);
	
	
	/**
	 * unapply is defined as a member method in order to allow `case object`s
	 * to have a value-based determination of whether or not the
	 * '''candidate''' logically identifies a ''specific'' '''DiscreteStatus'''.
	 */
	def unapply (candidate : AnyRef) : Boolean =
		candidate match {
			case s : String =>
				name equalsIgnoreCase (s);
				
			case ds : T =>
				name equals (ds.name);
				
			case s : Symbol =>
				name equals (s.name);
			}
	
	
	/**
	 * The unless method is the logical inverse of `when` and will only
	 * evaluate the given '''block''' if '''this''' status type does ''not''
	 * equal the '''unwanted''' instance.
	 */
	def unless[M[_]] (unwanted : T) : DiscreteStatus.BlockFunctor[M] =
		new DiscreteStatus.BlockFunctor[M] (!this.equals (unwanted));
	

	/**
	 * The when method will evaluate the given '''block''' if and only if
	 * '''this''' status type equals the '''desired''' instance.
	 */
	def when[M[_]] (desired : T) : DiscreteStatus.BlockFunctor[M] =
		new DiscreteStatus.BlockFunctor[M] (this.equals (desired));
}


object DiscreteStatus
{
	/// Class Types
	sealed class BlockFunctor[M[_]] (private val allow : Boolean)
	{
		/// Class Imports
		import syntax.std.boolean._
	
	
		def apply[R] (block : => R)
			(implicit A : Applicative[M], PE : PlusEmpty[M])
			: M[R] = allow.guard[M] (block);
	}
	
	
	/// Implicit Conversions
	implicit def DiscreteStatusEqual[A <: DiscreteStatus[A]]
		: Equal[DiscreteStatus[A]] =
		Equal.equalA[DiscreteStatus[A]];
}


trait DiscreteStatusImplicits[A <: DiscreteStatus[A]]
{
	/// Class Imports
	import text.format.Textualize


	/// Implicit Conversions
	/**
	 * The DiscreteStatusEqual `implicit` provides type-safe equality for derived
	 * [[com.github.osxhacker.foundation.models.core.DiscreteStatus]] types.  By
	 * producing an [[scalaz.Equal]] parameterized on `A`, distinct status
	 * categories are enforced by the compiler yet [[scalaz.Equal]] remains
	 * maximally useful.
	 */
	implicit val DiscreteStatusEqual : Equal[A] = Equal.equalA[A];
	
	implicit val DiscreteStatusTextualize : Textualize[A] =
		Textualize.by (_.name);
}

