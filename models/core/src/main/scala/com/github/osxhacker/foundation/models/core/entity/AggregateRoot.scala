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

import scala.reflect.ClassTag

import akka.actor._
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import akkax.extension.DomainEventBus
import error.ApplicationError


/**
 * The '''AggregateRoot''' type defines an [[akka.actor.FSM]] which provides
 * a well-defined `root` [[com.github.osxhacker.foundation.models.core.Entity]] and
 * explicit boundaries by way of it being a "share-nothing" abstraction.
 *
 * @author osxhacker
 */
abstract class AggregateRoot[RootT <: Entity[RootT] : ClassTag, S, D]
	extends FSM[S, D]
		with akkax.Slf4jLogging
{
	/// Class Imports
	import AggregateRoot.DefaultPublisher


	/// Instance Properties
	implicit val publisher : EventPublisher[RootT] =
		new DefaultPublisher[RootT] (context.system);

	protected def root : EntityRef[RootT];


	/**
	 * The preRestart method informs the `sender` of the last message received
	 * that '''this''' [[akka.actor.Actor]] crashed by way of emitting a
	 * [[akka.actor.Status.Failure]] response.
	 */
	override def preRestart (reason : Throwable, message : Option[Any]) : Unit =
	{
		sender () ! Status.Failure (reason);
		super.preRestart (reason, message);
	}


	/**
	 * The publish method allows '''AggregateRoot'''s to inform interested
	 * parties that a [[com.github.osxhacker.foundation.models.core.DomainEvent]]
	 * has transpired.
	 */
	def publish (event : DomainEvent) : Unit = publisher.publish (event);
}


object AggregateRoot
{
	/// Class Types
	final class DefaultPublisher[RootT <: Entity[RootT]] (
		private val system : ActorSystem
		)
		extends EventPublisher[RootT]
	{
		override def publish (event : DomainEvent) : Unit =
			DomainEventBus (system).publish (event);
	}
}
