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

import scala.concurrent.{
	Await,
	ExecutionContext,
	Future
	}

import scala.language.{
	higherKinds,
	postfixOps
	}

import akka.stream.scaladsl.Source
import org.scalatest.DiagrammedAssertions
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec
import com.github.osxhacker.foundation.models.core.akkax.ImmediateSource
import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''UnwrapMASpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.core.functional.UnwrapMA]] generic type for
 * use in generic constructs as well as serving to be an exemplar.
 *
 * @author osxhacker
 *
 */
final class UnwrapMASpec
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import ExecutionContext.Implicits.global
	import syntax.all._


	"UnwrapMA" must {
		"support unwrapping Lists" in {
			val l = 1 :: 2 :: 3 :: Nil;
			val u1 = unwrap (l);

			assert (u1 === l);

			assertCompiles ("""
				val keptType : Int = u1.head;
				""");
			}

		"support unwrapping futures" in {
			val fi = Future (1);
			val u = unwrap (fi);

			assertCompiles ("""
				val i : Int = Await.result (u, ???);
				"""
				);
			}

		"support unwrapping scalaz eithers" in {
			val e = "foo".right[ApplicationError];
			val u1 = unwrap (e);

			assert (u1.toOption === e.toOption);

			assertCompiles ("""
				val keptType : String = u1.valueOr (e => throw e);
				""");
			}

		"support unwrapping sources" in {
			val s = Source.single (1);
			val u = unwrap (s);

			assertCompiles ("""
				val keptType : ImmediateSource[Int] = u;
				""");
			}
		}


	private def unwrap[MA, M[_], A] (ma : MA)
		(implicit U : UnwrapMA[MA, M, A])
		: M[A] =
		U (ma);
}
