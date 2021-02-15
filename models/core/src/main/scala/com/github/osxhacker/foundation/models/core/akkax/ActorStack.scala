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

import scala.concurrent.duration.Duration

import akka.actor._


/**
 * The '''ActorStack''' type defines an `Actor` which can have behaviour
 * added to it in an incremental fashion.  It does so by decorating the
 * concrete implementation's `receive` method by way of `stacked` traits.
 * Ultimately, the one defined in the '''ActorStack''' here will be invoked,
 * which calls the concrete `receive` method if and only if ''it'' can
 * process the message.
 * 
 * ==IMPLEMENTATION NOTE==
 * 
 * If the concrete type wants to employ contextual `Receive` handlers, a la
 * `context.become` and `context.unbecome`, the versions provided in ''this''
 * class ''must'' be used.  Otherwise, the
 * [[com.github.osxhacker.foundation.models.core.akkax.Stackable]] behaviour will
 * be lost!
 *
 * @author osxhacker
 *
 */
abstract class ActorStack
	extends Stackable
{
	override def preStart () : Unit =
	{
		super.preStart ();

		context.become (decorateReceiver (receive));
	}
	
	
	/**
	 * This version of the stacked method is a no-op and exists so that the
	 * [[com.github.osxhacker.foundation.models.core.akkax.Stackable]]
	 * implementations *always* have a parent to call.
	 */
	override def stacked : Decorator = new Decorator {
		override def apply (msg : Any) = {}
		}
	
	
	/**
	 * Use this become method when looking to switch message `Receive`
	 * `behaviour` in derived types.  Its signature is intentionally a "drop in"
	 * replacement of that found in `ActorContext`.
	 */
	def become (behaviour : Receive, discardOld : Boolean = true) : Unit =
		context.become (decorateReceiver (behaviour), discardOld);
	
	
	/**
	 * The unbecome method is provided for completeness and delegates its
	 * implementation to the `Actor`s `context`.
	 */
	def unbecome () : Unit = context.unbecome ();

	
	private def decorateReceiver (receiver : Receive) = new Receive {
		override def isDefinedAt (msg : Any) = true;
		override def apply (msg : Any) = {
			stacked (msg);
			receiver.applyOrElse (msg, unhandled);
			}
		}
}

