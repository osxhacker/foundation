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

import scala.concurrent.Future
import scala.concurrent.duration.Deadline
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.{
	Failure,
	Success,
	Try
	}

import akka.actor.{
	ActorRef,
	ActorRefFactory
	}

import akka.pattern.ask
import akka.util.Timeout
import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.functional.futures
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.core.error.ApplicationError

import error.ActorRequestError


/**
 * The '''MessagingClient''' type defines a simple
 * [[com.github.osxhacker.foundation.models.core.akkax.MessagingExtensionLike]]
 * proxy for an [[akka.actor.ActorRef]] which must respond to the
 * specified `RequestT` and produce `ResponseT` answers.
 *
 * @author osxhacker
 */
class MessagingClient[
	ResponseT[A <: ResponseT[A]] <: Response[A],
	RequestT[A <: ResponseT[A]] <: Request[A]
	] (override protected val actor : ActorRef)
	(implicit protected val factory : ActorRefFactory)
	extends MessagingExtensionLike
{
	/// Class Imports
	import EitherT.eitherT
	import factory.dispatcher
	import futures.monad._
	import syntax.bifunctor._
	import syntax.either._


	/// Class Types
	override type ResponseType[A <: ResponseType[A]] = ResponseT[A]
	override type RequestType[A <: ResponseType[A]] = RequestT[A]


	override def ![A <: Message[A]] (request : Message[A]) : Unit =
		actor ! request;


	override def ?[R <: ResponseT[R]] (request : RequestT[R])
		(implicit D : Deadline, CT : ClassTag[R])
		: FutureEither[R] =
		eitherT {
			ask (actor, request) (Timeout (D.timeLeft)).transform (
				liftResponse (request)
				);
			}
	
	
	/**
	 * The autoSend method will ensure that the '''MessagingClient''' will
	 * produce the given '''message''' when the '''master''' terminates.  It is
	 * the complement of the `watch` method.
	 * 
	 * {{{
	 * val master : ActorRef = ...
	 * val client = createMessagingclient ();
	 * 
	 * client.autoSend (master, MessageForTheClient ());
	 * 
	 * ...
	 * 
	 * /// when `master` terminates, `client` will be sent the
	 * /// MessageForTheClient instance automatically.
	 * master ! PoisonPill ();
	 * }}}
	 */
	def autoSend[A <: Message[A]] (master : ActorRef, message : A) : Unit =
		AutoSendMessage (master, actor, message);


	/**
	 * The watch method ensures that when the `actor` managed by this
	 * '''MessagingClient''' terminates, the '''master''' will receive the
	 * given '''message''' unconditionally.  It is the complement of the
	 * `autoSend` method.
	 * 
	 * {{{
	 * val master : ActorRef = ...
	 * val client = createMessagingclient ();
	 * 
	 * client.watch (master, MessageForTheMaster ());
	 * 
	 * ...
	 * 
	 * /// when `client` terminates, `master` will be sent the
	 * /// MessageForTheMaster instance automatically.
	 * client ! PoisonPill ();
	 * }}}
	 */
	def watch[A <: Message[A]] (master : ActorRef, message : A) : Unit =
		AutoSendMessage (actor, master, message);


	private def liftResponse[R <: ResponseT[R]] (request : RequestT[R])
		(implicit CT : ClassTag[R])
		: Try[Any] => Try[ApplicationError \/ R] =
		_ match {
			case Success (r) =>
				Success[ApplicationError \/ R] (
					CT.runtimeClass
						.cast (r)
						.asInstanceOf[R]
						.right
					);

			case Failure (cause) =>
				Success[ApplicationError \/ R] (
					ActorRequestError (actor, request, cause).left
					);
			}
}


object MessagingClient
{
	/**
	 * The apply method provides functional creation semantics for producing
	 * a '''MessagingClient''' instance.
	 */
	def apply[
		ResponseT[X <: ResponseT[X]] <: Response[X],
		RequestT[X <: ResponseT[X]] <: Request[X]
		] (actor : ActorRef)
		(implicit factory : ActorRefFactory)
		: MessagingClient[ResponseT, RequestT] =
		new MessagingClient[ResponseT, RequestT] (actor);
}

