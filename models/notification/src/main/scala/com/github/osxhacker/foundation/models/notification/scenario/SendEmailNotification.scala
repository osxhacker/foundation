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

package com.github.osxhacker.foundation.models.notification.scenario

import scala.concurrent.duration.Deadline

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.akkax.extension.SystemWideMaterializer
import com.github.osxhacker.foundation.models.core.entity.EntityRef
import com.github.osxhacker.foundation.models.core.functional._
import com.github.osxhacker.foundation.models.notification.EmailAddress
import com.github.osxhacker.foundation.models.notification.email._
import com.github.osxhacker.foundation.models.notification.akkax.extension.EmailNotificationLocator

import akkax.{
	ActorSystemAware,
	MaterializerAware
	}


/**
 * The '''SendEmailNotification''' type defines a Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] which produces a functor that
 * will `send` each [[com.github.osxhacker.foundation.models.notification.email.Envelope]]
 * provided to it.
 * 
 * Note that the
 * [[com.github.osxhacker.foundation.models.notification.email.EmailNotification]] service
 * is only resolved '''once'''.
 *
 * @author osxhacker
 *
 */
final case class SendEmailNotification ()
	(implicit override val system : ActorSystem, private val d : Deadline)
	extends Scenario[Flow[Envelope, EntityRef[Email], NotUsed]]
		with ActorSystemAware
		with MaterializerAware
{
	/// Class Imports
	import akkax.streams._
	import futures.monad._
	import syntax.monad._
	import system.dispatcher


	/// Instance Properties
	implicit override val materializer =
		SystemWideMaterializer (system).materializer;

	private lazy val service = EmailNotificationLocator (system).locate ();
	private lazy val steps = Flow[Envelope].zip {
		Source.fromFuture (service.run)
			.valueOrFail ()
		}
		.mapExpand {
			case (envelope, service) =>

			Source.single (envelope)
				.via (service.send ());
			}


	override def apply () : Flow[Envelope, EntityRef[Email], NotUsed] = steps;
}
