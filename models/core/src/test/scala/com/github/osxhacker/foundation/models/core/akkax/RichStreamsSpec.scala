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

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import org.scalatest.DiagrammedAssertions
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import org.scalatest.WordSpecLike

import com.github.osxhacker.foundation.models.core.functional._


/**
 * The '''RichStreamsSpec''' type defines the unit tests for the
 * [[https://doc.akka.io/docs/akka/current/stream/index.html?language=scala Akka Streams]]
 * type classes.
 *
 * @author osxhacker
 *
 */
final class RichStreamsSpec
	extends ActorBasedSpec ("test-rich-streams")
		with DiagrammedAssertions
		with WordSpecLike
{
	/// Class Imports
	import RichStreamsSpec._
	import system.dispatcher
	import futures.comonad._
	import implicits._
	import streams._
	import std.anyVal.intInstance
	import std.list._
	import std.option._
	import std.set._
	import std.vector._
	import syntax.comonad._


	/// Instance Properties
	implicit val materializer = ActorMaterializer ();


	"RichSource" must {
		"be able to produce a FutureEither" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.single (42).toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_ === 42));
			}

		"fail to produce a FutureEither when empty" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.empty[Long].toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isLeft);
			}

		"gracefully handle errors in the stream" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.single (0)
				.map (1 / _)
				.toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isLeft);
			}

		"be able to produce a monoid FutureEither" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.single (42).toFutureEitherM[Option] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.exists (_ === 42)));
			}

		"be able to produce a FutureEither collection" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.repeat (42)
				.take (20)
				.toFutureEitherCollection[Vector] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.forall (_ === 42)));
			assert (result.exists (_.size === 20));
			}

		"consume all elements of a collection" in {
			implicit val deadline = 1 second fromNow;
			val ints = List.fill (50) (9);

			val future = Source.single (ints)
				.mapExpand (identity)
				.toFutureEitherCollection[Vector] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (
				result.exists (_.size === ints.size),
				"expected %d items, had %d".format (
					ints.size,
					result.map (_.size).valueOr (e => 0)
					)
				);
			}

		"consume all elements of a collection (Monoid)" in {
			import std.list._

			implicit val deadline = 1 second fromNow;
			val ints = List.fill (50) (9);

			val future = Source.fromIterator (() => ints.iterator)
				.toFutureEitherM[List] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.size === ints.size));
			}

		"produce an empty FutureEither collection when empty" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.empty[String]
				.toFutureEitherCollection[List] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.isEmpty));
			}
		
		"gracefully handle errors in a collection stream" in {
			implicit val deadline = 1 second fromNow;

			val future = Source (-5 to 5)
				.map (1 / _)
				.toFutureEitherCollection[List] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isLeft);
			}

		"be able to 'map reduce'" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.repeat (1)
				.take (20)
				.mapReduce (2 * _)
				.toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_ === 40), result.toString);
			}
		
		"be able to 'map expand'" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.single ("abcdef")
				.mapExpand (_.to[List])
				.toFutureEitherCollection[Set] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.size === 6));
			assert (result.exists (_.contains ('e')));
			}

		"be able to 'requestOf' MessageClient's" in {
			implicit val duration = 1 second;
			implicit val deadline = duration fromNow;

			val client = MessagingClient[SampleResponse, SampleRequest] (
				system.actorOf (Props (new MessageBasedActor ()))
				);

			val future = Source.single (4)
				.requestOfClient (client) {
					n =>

					SquareIntRequest (n);
					}
				.toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_ === SquareIntResponse (16)));
			}
		}
	
	"RichFlow" must {
		"be able to 'map reduce'" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.repeat (1)
				.take (10)
				.via (Flow.apply[Int].mapReduce (5 * _))
				.toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_ === 50), result.toString);
			}
		
		"be able to 'map expand'" in {
			implicit val deadline = 1 second fromNow;

			val future = Source.single ("abcdefffffffff")
				.via (
					Flow[String]
						.mapExpand (_.to[List])
						.mapReduce (c => Set (c))
					)
				.toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.size === 6));
			assert (result.exists (_.contains ('e')));
			}

		"consume all elements of a collection" in {
			implicit val deadline = 1 second fromNow;
			val ints = List.fill (50) (9);

			val future = Source.fromIterator (() => ints.iterator)
				.via (Flow.fromFunction (_.toString))
				.toFutureEitherCollection[Vector] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.size === ints.size));
			}

		"consume all elements of a collection (Monoid)" in {
			import std.list._

			implicit val deadline = 1 second fromNow;
			val ints = List.fill (50) (9);

			val future = Source.fromIterator (() => ints.iterator)
				.via (Flow.fromFunction (_.toString))
				.toFutureEitherM[List] ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_.size === ints.size));
			}

		"be able to 'requestOf' MessageClient's" in {
			implicit val duration = 1 second;
			implicit val deadline = duration fromNow;

			val client = MessagingClient[SampleResponse, SampleRequest] (
				system.actorOf (Props (new MessageBasedActor ()))
				);

			val future = Source.single (2)
				.via (
					Flow[Int].requestOfClient (client) {
						n =>

						SquareIntRequest (n);
						}
					)
				.toFutureEither ();

			assert (future ne null);

			val result = future.run copoint;

			assert (result.isRight);
			assert (result.exists (_ === SquareIntResponse (4)));
			}
		}
}


object RichStreamsSpec
{
	/// Class Types
	sealed trait SampleRequest[R <: SampleResponse[R]]
		extends Request[R]


	sealed trait SampleResponse[R <: SampleResponse[R]]
		extends Response[R]


	case class SquareIntRequest (val input : Int)
		extends SampleRequest[SquareIntResponse]


	case class SquareIntResponse (val output : Int)
		extends SampleResponse[SquareIntResponse]


	final class MessageBasedActor ()
		extends Actor
	{
		override def receive : Receive =
		{
			case SquareIntRequest (a) =>
				sender () ! SquareIntResponse (a * a);
		}
	}
}
