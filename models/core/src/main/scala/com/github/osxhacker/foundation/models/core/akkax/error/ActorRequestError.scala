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

package com.github.osxhacker.foundation.models.core.akkax.error

import scala.language.postfixOps

import akka.actor._

import com.github.osxhacker.foundation.models.core.akkax.{
	Request,
	Response
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''ActorRequestError''' type defines an
 * [[com.github.osxhacker.foundation.models.core.error.ApplicationError]] raised
 * when communicating with an [[akka.actor.Actor]].
 *
 * @author osxhacker
 */
final case class ActorRequestError[A <: Response[A]] (
	val actor : ActorRef,
	val request : Request[A],
	override val cause : Option[Throwable]
	)
	extends ApplicationError (
		"Error sending request to %s: %s (cause=%s)".format (
			actor,
			request,
			cause map (_.getMessage)
			),
		cause
		)


object ActorRequestError
{
	def apply[A <: Response[A]] (actor : ActorRef, request : Request[A])
		: ActorRequestError[A] =
		new ActorRequestError[A] (actor, request, None);


	def apply[A <: Response[A]] (
		actor : ActorRef,
		request : Request[A],
		cause : Throwable
		) : ActorRequestError[A] =
		new ActorRequestError[A] (actor, request, Option (cause));
}

