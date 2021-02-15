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

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import akka.actor._
import akka.testkit.{
	ImplicitSender,
	TestKit
	}

import com.typesafe.config.{
	Config,
	ConfigFactory
	}

import org.scalatest._

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.DomainEvent
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither


/**
 * The '''ActorBasedSpec''' type provides support for both unit and integration
 * tests which use [[akka.actor.Actor]]s.  It ensures an
 * [[akka.actor.ActorSystem]] is available as well as ensuring its orderly
 * shutdown.
 * 
 * Concrete [[org.scalatest.Suite]]s have the option of providing both a
 * test-wide `application-test.conf` as well as a per-test one having the
 * resource name of `systemName + ".conf"`.
 *
 * @author osxhacker
 */
abstract class ActorBasedSpec (systemName : String)
	extends TestKit (
		ActorSystem (systemName, ActorBasedSpec.config (systemName))
		)
		with ImplicitSender
		with Suite
		with BeforeAndAfterAll
		with ActorSystemAware
		with DelayFor
{
	/// Class Imports
	import functional._
	import functional.futures.monad._
	import syntax.applicative._
	import syntax.either._
	import system.dispatcher


	/// Class Types
	implicit class EitherOps[E <: Throwable, A] (val self : E \/ A)
	{
		def orThrow : A = self valueOr (e => throw e);
	}


	implicit class SimulateOps[MC <: MessagingExtensionLike] (val client : MC)
	{
		/// Class Types
		type ResultType = FutureEither[MC]


		def simulate (message : FSM.StateTimeout.type) : ResultType =
			send (message);


		def simulate (event : DomainEvent) : ResultType =
			send (event);


		def simulate[A <: Message[A]] (event : Message[A]) : ResultType =
			send (event);


		private def send (message : Any) : ResultType =
		{
			client.withActor (_ ! message);

			return client.point[FutureEither];
			
		}
	}


	/// Instance Properties
	val StateTimeout = FSM.StateTimeout;


	override protected def afterAll () : Unit =
	{
		TestKit.shutdownActorSystem (system);
	}


	def expectClientTerminated[
		ResponseT[A <: ResponseT[A]] <: Response[A],
		RequestT[A <: ResponseT[A]] <: Request[A]
		]
		(
			client : MessagingClient[ResponseT, RequestT],
			max : Duration = Duration.Undefined
		)
		: Terminated =
		client.withActor (actor => expectTerminated (actor, max));


	def watchClient[
		ResponseT[A <: ResponseT[A]] <: Response[A],
		RequestT[A <: ResponseT[A]] <: Request[A]
		] (client : MessagingClient[ResponseT, RequestT])
		: MessagingClient[ResponseT, RequestT] =
	{
		client.withActor (actor => watch (actor));
		
		return client;
	}
}


object ActorBasedSpec
{
	/// Instance Properties
	val globalTestConfig = "application-test.conf";
	
	
	def config (systemName : String) : Config =
		ConfigFactory.load (systemName + ".conf")
			.withFallback (ConfigFactory.load (globalTestConfig))
			.withFallback (ConfigFactory.load ());
}
