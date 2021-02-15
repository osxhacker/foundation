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

package com.github.osxhacker.foundation.models.core.akkax

import scala.reflect.ClassTag

import akka.actor.Actor
import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.DomainEvent
import com.github.osxhacker.foundation.models.core.entity.EventSubscriber


/**
 * The '''ActorEventSubscriber''' type defines an [[akka.actor.Actor]]-based
 * [[com.github.osxhacker.foundation.models.core.entity.EventSubscriber]], which
 * uses the
 * [[com.github.osxhacker.foundation.models.core.akkax.extension.DomainEventBus]]
 * [[akka.actor.Extension]] to `subscribe` and `unsubscribe` for
 * [[com.github.osxhacker.foundation.models.core.DomainEvent]]'s `E` and any
 * derived types.
 *
 * @author osxhacker
 */
trait ActorEventSubscriber[E <: DomainEvent]
	extends EventSubscriber[E]
{
	/// Self Type Constraints
	this : Actor =>
	
	
	/// Class Imports
	import extension.DomainEventBus


	override def subscribe ()
		(implicit CT : ClassTag[E])
		: Unit =
		DomainEventBus (context.system).subscribe[E] (self);
	

	override def unsubscribe ()
		(implicit CT : ClassTag[E])
		: Unit =
		DomainEventBus (context.system).unsubscribe[E] (self);
}
