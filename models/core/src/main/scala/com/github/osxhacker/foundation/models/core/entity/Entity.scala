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

import scala.language.postfixOps
import scala.reflect.ClassTag

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import error.ApplicationError
import time.types.ReadableInstant


/**
 * The '''EntityLike''' type defines the contract for `trait`s and
 * `abstract class`es which are themselves parent types to an
 * [[com.github.osxhacker.foundation.models.core.entity.Entity]].  By having the contents
 * defined here, such as the `EntityLenses`, type hierarchies can express
 * relationships while being able to preserve system-wide constraints such
 * as `A <: Entity[A]`.
 * 
 * @author osxhacker
 * 
 */
trait EntityLike[T <: EntityLike[T]]
{
	/// Class Types
	type EntityType <: T

	final type ValidatedChange[+X] = Entity.ValidatedChange[X]
	
	
	protected trait EntityLenses
	{
		/**
		 * The lensFor method is a convenience method used to define
		 * [[scalaz.Lens]]es for [[EntityType]].
		 * 
		 * Sample use:
		 * {{{
		 * val field = lensFor[String] (
		 * 		(foo, v) => foo.copy (field = v),
		 *   	_.field
		 *	);
		 * }}}
		 */
		def lensFor[A] (
			set : (EntityType, A) => EntityType,
			get : EntityType => A
			)
			: EntityType @> A =
			Lens.lensu[EntityType, A] (set, get);
			
			
		/**
		 * The plensFor method is a convenience method used to define
		 * [[scalaz.PLens]]es (''partial'' lenses) for the [[EntityType]].
		 */
		def plensFor[A] (
			set : EntityType => Option[A => EntityType],
			get : EntityType => Option[A]
			)
			: EntityType @?> A =
			PLens.plensg[EntityType, A] (set, get);
		
		
		def id : EntityType @> Identifier;
		def timestamps : EntityType @?> ModificationTimes;

		def createdOn : EntityType @?> ReadableInstant =
			timestamps >=> ModificationTimes.lenses.createdOn;

		def lastChanged : EntityType @?> ReadableInstant =
			timestamps >=> ModificationTimes.lenses.lastChanged;
	}


	/// Instance Properties
	val self : EntityType;
	protected val lenses : EntityLenses;
}


/**
 * The '''Entity''' type defines the contract for ''all'' Domain Object Model
 * types which are uniquely identified not by their attributes, but instead by
 * an [[com.github.osxhacker.foundation.models.core.Identifier]] which transcends the
 * run-time lifecycle of the '''Entity'''.
 * 
 * Since each '''Entity''' has a unique
 * [[com.github.osxhacker.foundation.models.core.Identifier]], there exists one and
 * only one valid definition of the [[scala.Equals]] contract.  As such, it is
 * defined here and its methods are marked `final`.
 *
 * @author osxhacker
 */
abstract class Entity[T <: Entity[T] : ClassTag]
	extends Equals
		with EntityLike[T]
{
	/// Self Type Constraints
	this : T =>
		
		
	/// Instance Properties
	private lazy val entityClass = implicitly[ClassTag[T]].runtimeClass;
	
	
	/// Constructor
	import lenses._
	
	
	override final def canEqual (that : Any) : Boolean =
	{
		val obj = that.asInstanceOf[Entity[_]];

		return (obj ne null) && entityClass.isInstance (obj);
	}
	
	
	override final def equals (that : Any) : Boolean =
		canEqual (that) && id.get (self).equals (
			id.get (that.asInstanceOf[EntityType])
			);
	
	
	override final def hashCode () : Int = id.get (self).hashCode ();
	
	
	override def toString () : String = "%s(id=%s)".format (
		entityClass.getName,
		id.get (self)
		);
	
	
	/**
	 * The toRef method produces an
	 * [[com.github.osxhacker.foundation.models.core.entity.EntityRef]] from '''this'''
	 * instance.
	 */
	final def toRef () : EntityRef[T] = EntityRef[T] (id.get (self));
	
	
	/**
	 * The toTag method attempts to produce an
	 * [[com.github.osxhacker.foundation.models.core.entity.EntityTag]], succeeding when
	 * '''this''' instance has valid `timestamps`.
	 */
	final def toTag () : Option[EntityTag] =
		timestamps.get (self).map {
			times =>

			EntityTag (toRef, times.lastChanged);
			}
}


trait EntityImplicits
{
	/// Implicit Conversions
	implicit def EntityEqual[T <: Entity[T]] : Equal[T] = Equal.equalA[T];
	
	
	implicit def EntityShow[T <: Entity[T]] : Show[T] =
		Show.showFromToString[T];
}


object Entity
	extends EntityImplicits
{
	/// Class Types
	final type ValidatedChange[+X] = ApplicationError \/ X;
	
	
	/**
	 * The '''View''' type defines the root of all '''Entity''' concrete views
	 * in the system.  These types provide the ability to expose a ''select''
	 * subset of information to collaborators.
	 * 
	 * @author osxhacker
	 */
	abstract class View[A <: Entity[A]] (val entity : A)
	{
		/// Class Imports
		import entity.{
			lenses,
			self
			}
		
		
		def createdOn () : Option[ReadableInstant] =
			lenses.createdOn.get (self);

		def id () : Identifier = lenses.id.get (self);
		def lastChanged () : Option[ReadableInstant] =
			lenses.lastChanged.get (self);


		/// Java Accessors
		def getCreatedOn () : ReadableInstant = createdOn () orNull;
		def getId () : Identifier = id ();
		def getLastChanged () : ReadableInstant = lastChanged () orNull;
	}
}

