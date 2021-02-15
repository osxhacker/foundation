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

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.functional.futures
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.core.error.ApplicationError

import error.ActorRequestError


/**
 * The '''MessagingExtensionLike''' generic `trait` defines the contract for
 * ''all'' Akka extensions which define `Actor` request/response protocols
 * categorized by the `ResponseT` and `RequestT` parameters.
 * 
 * By providing this contract here, we regain type safety as quick as possible
 * and leverage the compiler's type checking ability to help catch `Actor`
 * collaborations which will never have a chance to succeed.  Specifically, if
 * a client sends a message to some actor expecting only a particular category
 * of messages, and that message doesn't "belong" to that type hierarchy, the
 * compiler will reject it immediately.
 *
 * @author osxhacker
 *
 */
trait MessagingExtensionLike
{
	/// Class Types
	type ResponseType[A <: ResponseType[A]] <: Response[A]
	type RequestType[A <: ResponseType[A]] <: Request[A]


	/// Instance Properties
	protected def actor : ActorRef;


	def ![A <: Message[A]] (request : Message[A]) : Unit;


	def ?[R <: ResponseType[R]] (request : RequestType[R])
		(implicit D : Deadline, CT : ClassTag[R])
		: FutureEither[R];


	/**
	 * The withActor method is available only to the `akkax` package so that
	 * implementations can be optimized by using the underlying '''actor'''.
	 */
	private[akkax] def withActor[A] (f : ActorRef => A) : A =
		f (actor);
}


/**
 * The '''MessagingExtension''' type defines the common behaviours of Akka
 * `Extension`s which define messaging protocols for system actors.  Logically,
 * it is a [[com.github.osxhacker.foundation.models.core.akkax.MessagingClient]] that
 * is also tagged as an [[akka.actor.Extension]].
 *
 * @author osxhacker
 *
 */
class MessagingExtension[
	ResponseT[A <: ResponseT[A]] <: Response[A],
	RequestT[A <: ResponseT[A]] <: Request[A]
	] (override protected val actor : ActorRef)
	(implicit override protected val factory : ActorRefFactory)
	extends MessagingClient[ResponseT, RequestT] (actor)
		with Extension

