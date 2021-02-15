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

package com.github.osxhacker.foundation.models.core.functional

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.{
	higherKinds,
	postfixOps
	}

import scala.math.Ordering

import akka.stream.scaladsl._
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

import com.github.osxhacker.foundation.models.core.akkax.ActorBasedSpec
import com.github.osxhacker.foundation.models.core.akkax.ImmediateSource
import com.github.osxhacker.foundation.models.core.akkax.extension.SystemWideMaterializer
import com.github.osxhacker.foundation.models.core.error.ApplicationError

import futures.FutureEither


/**
 * The '''ContainerFunctorSpec''' type defines the unit-tests which certify the
 * [[com.github.osxhacker.foundation.models.core.functional.containers]] set of
 * functionality.
 *
 * @author osxhacker
 *
 */
final class ContainerFunctorSpec
	extends ActorBasedSpec ("test-container-functor")
		with DiagrammedAssertions
		with WordSpecLike
{
	/// Class Imports
	import ExecutionContext.Implicits.global
	import containers._
	import futures.comonad._
	import std.set._
	import std.vector._
	import syntax.all._


	/// Class Types
	case class CustomContainer[A] (val a : A)


	object CustomContainer
	{
		implicit def customToOption[A]
			: ContainerFunctor[CustomContainer, Option, A] =
			new ContainerFunctor[CustomContainer, Option, A] {
				override def transform (fa : CustomContainer[A]) : Option[A] =
					Option (fa.a);
				}
	}


	/// Instance Properties
	implicit val materializer = SystemWideMaterializer (system).materializer;


	"ContainerFunctor" must {
		"obey its laws" in {
			val cf = ContainerFunctor[Vector, Set, Int] ();

			assert (cf.containerFunctorLaw.empty ());
			assert (cf.containerFunctorLaw.nonempty (42));
			}

		"be able to map a list into collections" in {
			val l = 1 :: 2 :: 2 :: 3 :: Nil;
			val empty = List.empty[String];

			check (l, l.toArray);
			check (l, l.toBuffer);
			check (l, Set (1, 2, 3));
			check (l, l.to[Vector]);
			check (empty, Array.empty[String]);
			check (empty, Seq.empty[String]);
			check (empty, Set.empty[String]);
			check (empty, Vector.empty[String]);
			}

		"be able to map an option into collections" in {
			val o = Option (42);

			assert (o.transform[List] () === List (42));

			check (o, o.toList);
			check (o, o.to[Set]);
			check (o, o.toVector);
			check (None : Option[Int], Array.empty[Int]);
			check (None : Option[Int], List.empty[Int]);
			check (None : Option[Int], Seq.empty[Int]);
			check (None : Option[Int], Set.empty[Int]);
			check (None : Option[Int], Vector.empty[Int]);
			}

		"be able to map Scalaz either's to FutureEither" in {
			implicit val deadline = 1 second fromNow;

			val ea = 3.0.right[ApplicationError];
			val result = ea.transform[FutureEither] ().run
				.copoint;

			assert (result.toOption === ea.toOption);
			}

		"be able to map collections to an ImmediateSource" in {
			val s = Set ("a", "bee", "c");
			val v = Vector (9, 2, 1, 1);

			checkToSource (s, s.toList);
			checkToSource (v, v.toList);
			}

		"support custom containers" in {
			val custom = CustomContainer ("some value");
			val result = custom.transform[Option] ();

			assert (result === Option (custom.a));
			}
		}


	private def check[F[_], G[_], A] (source : F[A], expected : G[A])
		(implicit CF : ContainerFunctor[F, G, A])
		: Unit =
		assert (source.transform[G] () === expected);


	private def checkToSource[F[_], A] (fa : F[A], expected : List[A])
		(
			implicit CF : ContainerFunctor[F, ImmediateSource, A],
			O : Ordering[A]
		)
		: Unit =
	{
		implicit val deadline = 1 second fromNow;

		val result = fa.transform[ImmediateSource] ()
			.runFold (List.empty[A]) {
				(accum, s) =>

				s :: accum;
				}
			.copoint;

		check (result.sorted, expected.sorted);
	}
}
