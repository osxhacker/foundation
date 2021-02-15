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

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import akka.testkit.TestActors
import org.scalatest.{
	Assertions,
	WordSpecLike
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''MessagingClientSpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.core.akkax.MessagingClient]] type for
 * fitness of purpose.
 *
 * @author osxhacker
 *
 */
final class MessagingClientSpec
	extends ActorBasedSpec ("test-messaging-client")
		with WordSpecLike
		with Assertions
{
	/// Class Imports


	/// Class Types
	sealed trait SampleRequest[R <: SampleResponse[R]]
		extends Request[R]
	
	sealed trait SampleResponse[R <: SampleResponse[R]]
		extends Response[R]
	
	case class SampleIntRequest (n : Int)
		extends SampleRequest[SampleIntResponse]
	
	case class SampleIntResponse (s : String)
		extends SampleResponse[SampleIntResponse]


	/// Instance Properties
	
	
	"A MessagingClient" must {
		"require a Request-derived type when summonning" in {
			assertDoesNotCompile ("""
				val client = MessagingClient[String, String] ();
				"""
				);
			}
		
		"summon a valid instance" in {
			val echo = system.actorOf (TestActors.echoActorProps);
			val client = MessagingClient[SampleResponse, SampleRequest] (echo);

			assert (client ne null);
			}
		}
}
