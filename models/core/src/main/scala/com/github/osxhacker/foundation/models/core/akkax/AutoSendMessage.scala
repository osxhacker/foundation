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

import akka.actor._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}


/**
 * The '''AutoSendMessage''' type defines a
 * [[com.github.osxhacker.foundation.models.core.akkax.Task]] which will automatically
 * send a canned [[com.github.osxhacker.foundation.models.core.akkax.Message]] when the
 * `linkTo` [[akka.actor.Actor]] terminates.  This is a model of the
 * [[https://doc.akka.io/docs/akka/current/actors.html#lifecycle-monitoring-aka-deathwatch]]
 * pattern described in the Akka documentation.
 *
 * @author osxhacker
 *
 */
class AutoSendMessage[A <: Message[A]] (
	private val linkTo : ActorRef,
	private val target : ActorRef,
	private val message : A
	)
	extends Task
{
	final override def receive : Receive =
	{
		case Terminated (`linkTo`) =>
			target ! message;

		case Terminated (`target`) =>
			context.stop (self);
	}
	
	
	final override def preStart () : Unit =
	{
		super.preStart ();

		context.watch (linkTo);
	}
}


object AutoSendMessage
{
	/**
	 * The apply method is provided to support functional-style creation of an
	 * '''AutoSendMessage''' instance.
	 */
	def apply[A <: Message[A]] (
		linkTo : ActorRef,
		target : ActorRef,
		message : A
		)
		(implicit factory : ActorRefFactory)
		: ActorRef =
		factory.actorOf (Props (new AutoSendMessage (linkTo, target, message)));
}
