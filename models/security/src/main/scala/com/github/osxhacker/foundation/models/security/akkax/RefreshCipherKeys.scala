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

package com.github.osxhacker.foundation.models.security.akkax

import scala.concurrent.duration.Deadline

import akka.actor._
import akka.pattern.pipe
import akka.stream.Materializer
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	DeploymentEnvironment,
	Identifier
	}

import com.github.osxhacker.foundation.models.core.akkax._
import com.github.osxhacker.foundation.models.core.error.DomainValueError
import com.github.osxhacker.foundation.models.core.functional.futures
import com.github.osxhacker.foundation.models.security.encryption.{
	CipherKeys,
	KeyStorage
	}

import com.github.osxhacker.foundation.models.security.error.VaultError


/**
 * The '''RefreshCipherKeys''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.akkax.Task]] responsible for "refreshing"
 * all known [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]]
 * from a [[com.github.osxhacker.foundation.models.security.encryption.KeyStorage]]
 * service.  If the
 * [[com.github.osxhacker.foundation.models.security.encryption.KeyStorage]] does not
 * produce an answer within the specified
 * [[scala.concurrent.duration.Deadline]], '''RefreshCipherKeys''' indicates a
 * timeout failure.
 *
 * @author osxhacker
 *
 */
final class RefreshCipherKeys (
	val requestor : ActorRef,
	override val when : Deadline
	)
	extends Task
		with DeadlineAware
{
	override def receive =
	{
		case DeadlinePassed =>
			requestor ! LoadingKeysResult (
				VaultError ("timeout refreshing keys")
				);

		case LoadingKeysResult (result) =>
			requestor ! LoadingKeysResult (
				result.ensure (DomainValueError ("no keys provided")) {
					_.isEmpty == false
					}
				);
	}
}


object RefreshCipherKeys
{
	/// Class Imports
	import futures.monad._
	import streams._


	/**
	 * The apply method is provided to support functional creation of the
	 * [[com.github.osxhacker.foundation.models.security.akkax.RefreshCipherKeys]]
	 * [[akka.actor.Actor]].
	 */
	def apply (
		requestor : ActorRef,
		deployment : DeploymentEnvironment,
		deadline : Deadline
		)
		(
			implicit factory : ActorRefFactory,
			materializer : Materializer,
			storage : KeyStorage
		)
		: ActorRef =
	{
		implicit val ec = factory.dispatcher;
		val task = factory.actorOf (
			Props (new RefreshCipherKeys (requestor, deadline))
			);

		storage.load (deployment)
			.toFutureEitherCollection[Seq] ()
			.run
			.map (result => task ! LoadingKeysResult (result))
			.pipeTo (task);

		return task;
	}
}

