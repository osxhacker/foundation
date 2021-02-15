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

package com.github.osxhacker.foundation.models.core.akkax.extension

import scala.concurrent.duration.Deadline
import scala.reflect.ClassTag

import akka.actor._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.{
	ActorSystemAware,
	DependencyContainer,
	MessagingClient
	}

import com.github.osxhacker.foundation.models.core.functional


/**
 * The '''DependencyLocator''' type defines the ability to use Akka
 * [[akka.actor.Extension]]s as a form of "dependency injection" mechanism.
 *
 * @author osxhacker
 *
 */
abstract class DependencyLocator[DependencyT <: AnyRef] ()
	(implicit override val system : ActorSystem, ct : ClassTag[DependencyT])
	extends Extension
		with ActorSystemAware
{
	/// Class Imports
	import DependencyContainer.messages._
	import functional._
	import functional.futures._
	import system.dispatcher


	/// Instance Properties
	private val container = MessagingClient[
		DependencyContainerResponse,
		DependencyContainerRequest
		] (DependencyContainer[DependencyT] ());


	/**
	 * The apply method attempts to resolve the ''DependencyT'' from the
	 * '''container''', if it has been provided previously, waiting for one
	 * to be provisioned if not.
	 */
	final def apply ()
		(implicit deadline : Deadline)
		: FutureEither[DependencyT] =
		(container ? LookupDependencyRequest[DependencyT] ()).flatMapE (
			_.result
			);


	/**
	 * The locate method is provided for syntactic convenience and is an alias
	 * for the `apply` method.
	 */
	final def locate ()
		(implicit deadline : Deadline)
		: FutureEither[DependencyT] =
		apply ();


	private[extension] def register (instance : DependencyT) : Unit =
		container ! RegisterDependency (instance);


	private[extension] def unregister () : Unit =
		container ! UnregisterDependency ();
}


/**
 * The '''DependencyProvider''' type defines common functionality needed to
 * initialize a concrete ''LocatorT'' as being an [[akka.actor.Extension]].
 * It is intended to be the parent to the ''LocatorT'' type's companion
 * `object`.
 *
 * @author osxhacker
 *
 */
abstract class DependencyProvider[
	DependencyT <: AnyRef,
	LocatorT <: DependencyLocator[DependencyT]
	] ()
	extends ExtensionId[LocatorT]
		with ExtensionIdProvider
{
	final override def lookup () = this;


	/**
	 * The register method provides the ability to provision an '''instance'''
	 * for use by the ''LocatorT''.
	 */
	final def register (instance : DependencyT)
		(implicit system : ActorSystem)
		: Unit =
		apply (system).register (instance);


	/**
	 * The unregister method allows the system to retract the availability
	 * of a ''DependencyT'' if one has been previously `register`ed.
	 */
	final def unregister ()
		(implicit system : ActorSystem)
		: Unit =
		apply (system).unregister ();
}

