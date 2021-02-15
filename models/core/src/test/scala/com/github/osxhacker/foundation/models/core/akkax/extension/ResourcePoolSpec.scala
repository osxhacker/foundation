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

import java.util.concurrent.atomic.AtomicInteger

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


/**
 * The '''ResourcePoolSpec''' type defines the unit-test suite which certifies
 * the [[com.github.osxhacker.foundation.models.core.akkax.extension.ResourcePool]]
 * [[akka.actor.Extension]] for fitness of purpose and serves as an exemplar
 * for its use.
 *
 * @author osxhacker
 *
 */
final class ResourcePoolSpec ()
	extends ActorBasedSpec ("test-resource-pool")
		with DiagrammedAssertions
		with WordSpecLike
{
	/// Class Imports
	import syntax.all._
	import system.dispatcher


	/// Class Types
	final case class SampleResource (val n : Int)


	final class SampleResourcePool ()
		extends ResourcePool[SampleResource] (min = 1, max = 10)
	{
		/// Class Types
		final class MyFactory ()
			extends ResourceFactory
		{
			/// Instance Properties
			private val counter = new AtomicInteger (1);

			override def create () : SampleResource =
				SampleResource (counter.incrementAndGet ());
		}


		override protected val factory = new MyFactory ();
	}

	
	object SampleResourcePool
		extends ExtensionId[SampleResourcePool]
			with ExtensionIdProvider
	{
		override def createExtension (system : ExtendedActorSystem)
			: SampleResourcePool =
			new SampleResourcePool ();


		override def lookup () = this;
	}

	/// Instance Properties
	implicit def deadline = 5 seconds fromNow;


	"The ResourcePool" must {
		"be able to create and use resources" in {
			val result = SampleResourcePool (system) (_.n);

			assert (result.isRight);
			assert (result.exists (_ > 0));
			}

		"handle exceptions gracefully" in {
			val result = SampleResourcePool (system) {
				_ =>

				throw new Exception ("simulated");
				}

			assert (result.isLeft);
			}
		}
}
