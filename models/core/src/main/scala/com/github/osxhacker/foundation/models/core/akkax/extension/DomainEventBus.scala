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

package com.github.osxhacker.foundation.models.core.akkax.extension

import scala.language.{
	higherKinds,
	postfixOps
	}

import scala.reflect.ClassTag

import akka.actor._
import akka.event.{
	EventBus,
	SubchannelClassification
	}

import akka.util.Subclassification
import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.DomainEvent


/**
 * The '''DomainEventBus''' type defines an [[akka.actor.Extension]] which
 * transports [[com.github.osxhacker.foundation.models.core.DomainEvent]]s by way of
 * a dedicated [[akka.event.EventBus]].
 *
 * @author osxhacker
 */
sealed class DomainEventBus (
	private val bus : DomainEventBus.EventTransport
	)
	extends Extension
{
	def publish[E <: DomainEvent] (event : E) : Unit = bus.publish (event);
	
	
	def subscribe[E <: DomainEvent : ClassTag] (actor : ActorRef) : Unit =
		bus.subscribe (actor, implicitly[ClassTag[E]].runtimeClass);
	
	
	def unsubscribe[E <: DomainEvent] (actor : ActorRef) : Unit =
		bus.unsubscribe (actor);
}


object DomainEventBus
	extends ExtensionId[DomainEventBus]
		with ExtensionIdProvider
{
	/// Class Types
	sealed class EventTransport
		extends EventBus
			with SubchannelClassification
	{
		/// Class Types
		type Event = DomainEvent
		type Classifier = Class[_]
		type Subscriber = ActorRef


		override protected def classify (event : Event) : Classifier =
			event.getClass;


		override protected def publish (
			event : Event,
			subscriber : Subscriber
			)
			: Unit =
			subscriber ! event;


		override def subclassification : Subclassification[Class[_]] =
			new Subclassification[Class[_]] {
				override def isEqual (a : Class[_], b : Class[_]) : Boolean =
					a == b;

				override def isSubclass (a : Class[_], b : Class[_])
					: Boolean =
					b isAssignableFrom (a);
				}
	}


	override def createExtension (system : ExtendedActorSystem)
		: DomainEventBus =
		new DomainEventBus (new EventTransport);
	
	
	override def lookup () = this;
}
