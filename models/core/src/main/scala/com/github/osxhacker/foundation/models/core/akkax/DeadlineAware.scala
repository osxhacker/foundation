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

import scala.concurrent.duration.Deadline

import akka.actor._


case object DeadlinePassed


/**
 * The '''DeadlineAware''' type specializes an [[akka.actor.Actor]] type by
 * producing a [[com.github.osxhacker.foundation.models.core.akkax.DeadlinePassed]]
 * message to '''this''' [[akka.actor.Actor]] if the '''duration''' has
 * expired.
 *
 * @author osxhacker
 *
 */
trait DeadlineAware
	extends Actor
{
	/// Class Imports
	import context.dispatcher


	/// Instance Properties
	val when : Deadline;

	@transient
	@volatile
	private var task : Option[Cancellable] = None;


	override def preStart () : Unit =
	{
		super.preStart ();

		task = Option (
			context.system.scheduler.scheduleOnce (when.timeLeft) {
				self ! DeadlinePassed
				}
			);
	}
	
	
	override def postStop () : Unit =
	{
		/// Ensure that the scheduled deadline task is cancelled so that
		/// delivery is not attempted after the Actor has stopped.
		cancel ();
		
		super.postStop ();
	}


	protected def cancel () : Unit =
	{
		task foreach (_.cancel);
		task = None;
	}
}

