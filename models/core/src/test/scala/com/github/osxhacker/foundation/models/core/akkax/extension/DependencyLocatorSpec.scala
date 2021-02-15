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

import scala.concurrent.duration._

import akka.actor._
import org.scalatest.{
	DiagrammedAssertions,
	WordSpecLike
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.{
	ActorBasedSpec,
	ActorSystemAware
	}

import com.github.osxhacker.foundation.models.core.functional


/**
 * The '''DependencyLocatorSpec''' type defines the unit tests which certify
 * the [[com.github.osxhacker.foundation.models.core.akkax.extension.DependencyLocator]]
 * functionality for fitness of purpose and serves as an exemplar for its use.
 *
 * @author osxhacker
 *
 */
final class DependencyLocatorSpec ()
	extends ActorBasedSpec ("test-dependency-locator")
		with DiagrammedAssertions
		with WordSpecLike
{
	/// Class Imports
	import functional.futures.comonad._
	import syntax.all._
	import system.dispatcher


	/// Class Types
	final class SampleLocator ()
		extends DependencyLocator[String] ()


	object SampleLocator
		extends DependencyProvider[String, SampleLocator] ()
	{
		override def createExtension (system : ExtendedActorSystem)
			: SampleLocator =
			new SampleLocator ();
	}


	/// Instance Properties
	implicit def deadline = 5 seconds fromNow;

	val instance = "hello world!";


	"A DependencyLocator" must {
		"be able to create an instance" in {
			assert (SampleLocator (system) ne null);
			}

		"be able to register a dependency" in {
			SampleLocator.register (instance);

			val result = SampleLocator (system).locate ()
				.run
				.copoint;

			assert (result.isRight);
			assert (result.exists (_ == instance));
			}

		"be able to unregister a dependency" in {
			SampleLocator.unregister ();
			}

		"be able to locate a dependency once one is registered" in {
			val future = SampleLocator (system).locate ();

			SampleLocator.register (instance);

			val result = future.run
				.copoint;

			assert (result.isRight);
			assert (result.exists (_ == instance));
			}
		}
}

