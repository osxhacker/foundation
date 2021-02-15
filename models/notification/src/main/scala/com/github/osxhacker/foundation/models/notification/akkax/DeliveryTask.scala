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

package com.github.osxhacker.foundation.models.notification.akkax

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor._
import akka.pattern.pipe
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax._
import com.github.osxhacker.foundation.models.core.error._
import com.github.osxhacker.foundation.models.core.scenario.GenerateUniqueIdentifier
import com.github.osxhacker.foundation.models.core.time
import com.github.osxhacker.foundation.models.notification.NotificationSettings
import com.github.osxhacker.foundation.models.notification.email.{
	Envelope,
	Receipt
	}

import com.github.osxhacker.foundation.models.notification.scenario.TransportEnvelope


/**
 * The '''DeliveryTask''' type defines the Akka
 * [[com.github.osxhacker.foundation.models.core.akkax.Task]] which is responsible for
 * delivering an [[com.github.osxhacker.foundation.models.notification.email.Envelope]]
 * via an
 * [[https://en.wikipedia.org/wiki/Message_transfer_agent MTA]] as specified
 * by the provided `settings`.
 *
 * @author osxhacker
 *
 */
final class DeliveryTask (
	private val requestor : ActorRef,
	private val envelope : Envelope,
	private val settings : NotificationSettings
	)
	(implicit ec : ExecutionContext)
	extends Task
		with ActorSystemAware
		with DeadlineAware
{
	/// Instance Properties
	implicit override val system = context.system;
	implicit override val when = settings.requestTimeout fromNow;

	private val receiptGenerator = GenerateUniqueIdentifier (Receipt.scheme);


	override def receive : Receive =
	{
		case DeadlinePassed =>
			requestor ! -\/ (TimeoutError ("email delivery timed out"));

		case \/- (_) =>
			requestor ! (receiptGenerator () map (id => Receipt (id))); 

		case -\/ (failure : ApplicationError) =>
			requestor ! -\/ (failure);
	}


	override def preStart () : Unit =
	{
		TransportEnvelope (envelope).run ()
			.run
			.pipeTo (self);
	}
}


object DeliveryTask
{
	/// Class Types


	/**
	 * The apply method is provided so that the details of how to instantiate a
	 * '''DeliveryTask''' instance is insulated from clients.
	 */
	def apply (
		requestor : ActorRef,
		envelope : Envelope,
		settings : NotificationSettings
		)
		(implicit factory : ActorRefFactory, ec : ExecutionContext)
		: ActorRef =
		factory.actorOf (
			Props (new DeliveryTask (requestor, envelope, settings))
			);
}
