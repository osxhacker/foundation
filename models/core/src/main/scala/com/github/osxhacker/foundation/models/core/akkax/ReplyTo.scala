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

import scala.concurrent.{
	ExecutionContext,
	Future
	}

import scala.util.{
	Failure,
	Success
	}

import akka.actor.Status
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import akka.actor.{
	Actor,
	ActorRef
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither


/**
 * The '''ReplyTo''' type provides `Actor`s with the ability to ''reply'' to
 * a collaborating `Actor` without running the risk of closing over the
 * `sender` of the original request.
 *
 * @author osxhacker
 *
 */
trait ReplyTo
{
	/// Self Type Constraints
	this : Actor =>


	/**
	 * This version of replyTo is suitable for situations where the production
	 * of the [[com.github.osxhacker.foundation.models.akkax.Response]] can be
	 * performed quickly.
	 */
	final protected def replyTo[A <: Response[A]] (recipient : ActorRef)
		(block : => A)
		: Unit =
		recipient ! block;


	/**
	 * This version of replyTo is for use when a `Future` result ('''fa''') will
	 * be transformed by '''f''' and ultimately sent to the '''recipient'''.
	 */
	final protected def replyTo[A, B <: Response[B]] (
		recipient : ActorRef,
		fa : Future[A]
		)
		(f : A => B)
		(implicit ec : ExecutionContext)
		: Unit =
		sendWhenComplete (recipient, fa, f);


	/**
	 * This version of replyTo is for use when a `FutureEither` result,
	 * '''fea'', will be transformed by '''f''' and ultimately sent to the
	 * '''recipient'''.
	 */
	final protected def replyTo[A, B <: Response[B]] (
		recipient : ActorRef,
		fea : FutureEither[A]
		)
		(f : ApplicationError \/ A => B)
		(implicit ec : ExecutionContext)
		: Unit =
		sendWhenComplete (recipient, fea.run, f);
	
	
	private def sendWhenComplete[A, B <: Response[B], R] (
		recipient : ActorRef,
		fr : Future[R],
		f : R => B
		)
		(implicit ec : ExecutionContext)
		: Unit =
		fr.onComplete {
			case Success (r) =>
				recipient ! f (r);

			case Failure (e) =>
				/// TODO: log error

				recipient ! Status.Failure (e);
			}
}

