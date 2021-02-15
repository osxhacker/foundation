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
package entity

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import error.{
	ApplicationError,
	DomainValueError
	}


/**
 * The '''EntityStatus''' type defines optional behaviour for
 * [[com.github.osxhacker.foundation.models.core.Entity]] types which have a
 * [[com.github.osxhacker.foundation.models.core.DiscreteStatus]] associated with
 * them.
 *
 * @author osxhacker
 */
trait EntityStatus[
	A <: EntityLike[A] with EntityStatus[A, B],
	B <: DiscreteStatus[B]
	]
{
	/// Self Type Constraints
	this : A
		with EntityStatus[A, B]
		=>


	/// Class Imports
	import syntax.applicativePlus._
	import syntax.equal._
	import syntax.std.boolean._


	/// Class Types
	protected trait StatusLens
	{
		this : EntityLenses =>

		def status : EntityType @> B;
	}


	override protected val lenses : EntityLenses with StatusLens;


	/**
	 * The changeStatus method applies the '''current''' status to '''this'''
	 * entity, returning the computation as a [[scalaz.State]] to be evaluated
	 * in context at a future time.
	 */
	def changeStatus (current : B) : State[EntityType, B] =
		lenses.status <:= current


	/**
	 * The whenAnyStatusOf method will evaluate the given '''steps''' if the
	 * current `status` is in the '''desired''' values.  Otherwise, a
	 * [[com.github.osxhacker.foundation.models.core.error.DomainValueError]] is
	 * produced.
	 */
	def whenAnyStatusOf[M[_]] (desired : Set[B])
		(steps : State[EntityType, EntityType])
		(implicit ME : MonadError[M, ApplicationError])
		: M[EntityType] =
		desired.contains (lenses.status.get (self)).fold (
			steps.eval (self).point[M],
			ME.raiseError (DomainValueError ("Status did not match"))
			);


	/**
	 * The whenStatusOf method will evaluate the given '''steps''' if the
	 * current `status` is the '''desired''' value.  Otherwise, a
	 * [[com.github.osxhacker.foundation.models.core.error.DomainValueError]] is
	 * produced.
	 */
	def whenStatusOf[M[_]] (desired : B)
		(steps : State[EntityType, EntityType])
		(implicit ME : MonadError[M, ApplicationError])
		: M[EntityType] =
		(lenses.status.get (self) == desired).fold (
			steps.eval (self).point[M],
			ME.raiseError (DomainValueError ("Status did not match"))
			);


	/**
	 * The unlessAnyStatusOf method will evaluate the given '''steps''' if the
	 * current `status` is __not__ in the '''unwanted''' values.  Otherwise, a
	 * [[com.github.osxhacker.foundation.models.core.error.DomainValueError]] is
	 * produced.
	 */
	def unlessAnyStatusOf[M[_]] (unwanted : Set[B])
		(steps : State[EntityType, EntityType])
		(implicit ME : MonadError[M, ApplicationError])
		: M[EntityType] =
		unwanted.contains (lenses.status.get (self)).fold (
			ME.raiseError (DomainValueError ("Status was undesired value")),
			steps.eval (self).point[M]
			);


	/**
	 * The unlessStatusOf method will evaluate the given '''steps''' if the
	 * current `status` is ''not'' the '''unwanted''' value.  Otherwise, a
	 * [[com.github.osxhacker.foundation.models.core.error.DomainValueError]] is
	 * produced.
	 */
	def unlessStatusOf[M[_]] (unwanted : B)
		(steps : State[EntityType, EntityType])
		(implicit ME : MonadError[M, ApplicationError])
		: M[EntityType] =
		(lenses.status.get (self) == unwanted).fold (
			ME.raiseError (DomainValueError ("Status was undesired value")),
			steps.eval (self).point[M]
			);
}


object EntityStatus
{
	/// Class Types
	trait View[
		A <: Entity[A] with EntityStatus[A, B],
		B <: DiscreteStatus[B]
		]
	{
		/// Self Type Constraints
		this : Entity.View[A] =>


		def status () : B = entity.lenses.status.get (entity.self);


		/// Java Accessors
		def getStatus () : B = status ();
	}
}

