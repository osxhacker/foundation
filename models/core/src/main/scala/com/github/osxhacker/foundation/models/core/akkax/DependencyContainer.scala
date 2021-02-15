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

import scala.reflect.ClassTag

import akka.actor._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error._


/**
 * The '''DependencyContainer''' type defines an [[akka.actor.FSM]] responsible
 * for managing a ''DependencyT'' by way of a concrete
 * [[com.github.osxhacker.foundation.models.core.akkax.extension.DependencyLocator]].
 *
 * @author osxhacker
 *
 */
final class DependencyContainer[DependencyT <: AnyRef] ()
	(implicit private val ct : ClassTag[DependencyT])
	extends FSM[DependencyContainer.State, DependencyContainer.Context]
{
	/// Class Imports
	import DependencyContainer._
	import DependencyContainer.messages._


	/// Constructor Body
	startWith (Missing, Pending ());


	/// Missing is the state entered into when a ''DependencyT'' has not
	/// been provided or has been explicitly retracted.
	when (Missing) {
		case Event (_ : LookupDependencyRequest[DependencyT], pending : Pending) =>
			stay () using (sender () +: pending);

		case Event (RegisterDependency (value : DependencyT), pending : Pending) =>
			pending.requestors foreach {
				_ ! LookupDependencyResponse (value);
				}

			goto (Provisioned) using (Dependency (value));

		case Event (_ : UnregisterDependency, _) =>
			stay ();
		}


	/// Provisioned is the state activated when the container has an instance
	/// of ''DependencyT'' to provide.
	when (Provisioned) {
		case Event (
			_ : LookupDependencyRequest[DependencyT],
			Dependency (value)
			) =>
			sender () ! LookupDependencyResponse (value);
			stay ();

		case Event (RegisterDependency (value : DependencyT), _) =>
			stay () using (Dependency (value));

		case Event (_ : UnregisterDependency, _) =>
			goto (Missing) using (Pending ());
		}


	initialize ();
}


object DependencyContainer
{
	/// Class Imports
	import syntax.either._


	/// Class Types
	sealed trait State


	case object Provisioned
		extends State


	case object Missing
		extends State


	sealed trait Context


	case class Pending (val requestors : List[ActorRef] = Nil)
		extends Context
	{
		def +: (requestor : ActorRef) : Pending =
			copy (requestors = requestor +: requestors);
	}


	case class Dependency[A <: AnyRef] (val value : A)
		extends Context


	object messages
	{
		sealed trait DependencyContainerMessage
			extends Message[DependencyContainerMessage]


		sealed trait DependencyContainerRequest[
			R <: DependencyContainerResponse[R]
			]
			extends Request[R]


		sealed trait DependencyContainerResponse[
			R <: DependencyContainerResponse[R]
			]
			extends Response[R]


		case class LookupDependencyRequest[A <: AnyRef] ()
			extends DependencyContainerRequest[LookupDependencyResponse[A]]


		case class LookupDependencyResponse[A <: AnyRef] (
			val result : ApplicationError \/ A
			)
			extends DependencyContainerResponse[LookupDependencyResponse[A]]


		object LookupDependencyResponse
		{
			def apply[A <: AnyRef] (value : A) : LookupDependencyResponse[A] =
				new LookupDependencyResponse[A] (value.right[ApplicationError]);
		}


		case class RegisterDependency[A <: AnyRef] (val value : A)
			extends DependencyContainerMessage


		case class UnregisterDependency ()
			extends DependencyContainerMessage
	}


	/**
	 * The apply method is provided to support functional-style creation of
	 * a [[com.github.osxhacker.foundation.models.core.akkax.DependencyContainer]].
	 */
	def apply[A <: AnyRef] ()
		(implicit factory : ActorRefFactory, ct : ClassTag[A])
		: ActorRef =
		factory.actorOf (Props (new DependencyContainer[A]));
}

