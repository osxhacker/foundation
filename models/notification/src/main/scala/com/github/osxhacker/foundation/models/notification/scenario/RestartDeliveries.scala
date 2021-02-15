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

import scala.concurrent.ExecutionContext

import akka.actor._
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
import com.github.osxhacker.foundation.models.core.akkax.{
	ActorSystemAware,
	MaterializerAware
	}

import com.github.osxhacker.foundation.models.core.akkax.extension.SystemWideMaterializer
import com.github.osxhacker.foundation.models.core.entity.EntityRef
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	ResourceUsageError
	}

import com.github.osxhacker.foundation.models.core.functional.futures._
import com.github.osxhacker.foundation.models.notification.akkax.SendEmail
import com.github.osxhacker.foundation.models.notification.email._
import com.github.osxhacker.foundation.models.notification.repository.EmailRepository


/**
 * The '''RestartDeliveries''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for initiating
 * a [[com.github.osxhacker.foundation.models.notification.akkax.SendEmail]] for each
 * [[com.github.osxhacker.foundation.models.notification.email.Email]] having a
 * qualifying status which indicates the need to have it sent.
 *
 * @author osxhacker
 *
 */
final case class RestartDeliveries ()
	(
		implicit override val system : ActorSystem,
		emailRepository : EmailRepository,
		ec : ExecutionContext
	)
	extends Scenario[FutureEither[Vector[EntityRef[Email]]]]
		with ActorSystemAware
		with MaterializerAware
{
	/// Class Imports
	import akkax.streams._
	import syntax.all._


	/// Instance Properties
	implicit override val materializer =
		SystemWideMaterializer (system).materializer;


	override def apply () : FutureEither[Vector[EntityRef[Email]]] =
		emailRepository.query (DeliveryIsInProgress)
			.concat (emailRepository.query (DeliveryIsPending))
			.runFold (Vector.empty[EntityRef[Email]]) {
				(accum, email) =>

				SendEmail (email.toRef);

				accum :+ email.toRef;
				}
			.map (_.right[ApplicationError])
			.recover {
				case e =>

				-\/ (ResourceUsageError ("email query failed", Some (e)));
				}
			.toFutureEither;
}
