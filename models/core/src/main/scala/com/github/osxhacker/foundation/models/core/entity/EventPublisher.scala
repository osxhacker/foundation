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

import scala.language.higherKinds

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import functional.Kestrel


/**
 * The '''EventPublisher''' type defines a
 * [[http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html type class]]
 * contract for being able to emit
 * [[com.github.osxhacker.foundation.models.core.DomainEvent]]s for any given
 * [[com.github.osxhacker.foundation.models.core.Entity]].
 * 
 * See also [[com.github.osxhacker.foundation.models.core.entity.EventSubscriber]].
 *
 * @author osxhacker
 */
trait EventPublisher[A <: Entity[A]]
{
	/**
	 * The publish method is expected to be effectual and interact with
	 * external agents (such as an event system).
	 */
	def publish (event : DomainEvent) : Unit;
}


trait EventPublisherAware
{
	protected def fireEvent[M[_], A <: Entity[A]] (f : A => DomainEvent)
		(implicit EP : EventPublisher[A], K : Kestrel[M], F : Functor[M])
		: M[A] => M[A] =
		fa => K.kestrel (fa) {
			a =>
				
			EP.publish (f (a));
			}
}
