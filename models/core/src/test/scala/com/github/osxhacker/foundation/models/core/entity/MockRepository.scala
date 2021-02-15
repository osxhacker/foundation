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

package com.github.osxhacker.foundation.models.core.entity

import scala.concurrent.Future

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error._


/**
 * The '''MockRepository''' type defines supporting behaviour for mock
 * [[com.github.osxhacker.foundation.models.core.entity.Repository]] types which use a
 * [[scala.collection.mutable.HashMap]] for the simulated persistent store.
 * To support thread-safe use, each interaction with the `underlying` ''Map''
 * is guarded by the `withStore` method.
 *
 * @author osxhacker
 *
 */
abstract class MockRepository[A <: Entity[A]] (
	private val initial : Seq[A] = Nil
	)
{
	/// Self Type Constraints
	this : Repository[A] =>


	/// Class Imports
	import State._
	import syntax.all._
	import syntax.std.all._


	/// Class Types
	type StorageType = Map[EntityRef[A], A]


	/// Instance Properties
	private var underlying : StorageType = initial.map {
		e =>

		e.toRef -> e;
		}
		.toMap;


	override def delete () : Sink[A, Future[IOResult]] =
		Sink.fold (IOResult.createSuccessful (0L)) {
			case (io, entity) =>

			val inc = withStore {
				for {
					_ <- init
					existed <- gets[StorageType, Boolean] (_.contains (entity.toRef))
					_ <- modify[StorageType] (_ - entity.toRef)
					} yield existed ? 1 | 0;
				}
			
			IOResult.createSuccessful (io.count + inc);
			}


	override def find (ref : EntityRef[A])
		: Source[A, NotUsed] =
		withStore {
			for {
				_ <- init
				entry <- gets[StorageType, Option[A]] (_.get (ref))
				} yield entry.cata (
					Source.single _,
					Source.failed (
						InvalidModelStateError ("missing entity")
						)
					);
			}


	override def findTag (ref : EntityRef[A])
		: Source[EntityTag, NotUsed] =
		find (ref).map (_.toTag.get);


	override def save () : Flow[A, A, NotUsed] = Flow.fromFunction[A, A] {
		entity =>

		withStore {
			for {
				_ <- init
				_ <- modify[StorageType] (_.updated (entity.toRef, entity))
				} yield entity;
			}
		}


	def empty () : Unit =
		withStore {
			for {
				_ <- init
				_ <- put (Map.empty[EntityRef[A], A])
				} yield ();
			}


	def repopulateWith (entities : Traversable[A]) : Unit =
		withStore {
			constantState (
				(),
				entities.map {
					entity =>

					entity.toRef -> entity;
					}
					.toMap
				);
			}


	protected def withStore[B] (operations : State[StorageType, B]) : B =
		synchronized {
			val (updated, result) = operations.run (underlying);

			underlying = updated;
			
			return result;
			}
}
