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

import scala.language.{
	higherKinds,
	postfixOps
	}

import scala.collection.mutable.Buffer

import org.scalatest.DiagrammedAssertions
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''FilterableSpec''' type defines the unit-tests which certify the
 * [[com.github.osxhacker.foundation.models.core.functional.Filterable]] type class for
 * fitness of purpose and serves as an exemplar of its use.
 *
 * @author osxhacker
 *
 */
final class FilterableSpec ()
	extends ProjectSpec
		with DiagrammedAssertions
{
	"The Filterable type class" must {
		"support Option types" in {
			val even : Option[Int] = filterNot (Option (42)) {
				_ % 2 == 1;
				}

			val odd : Option[Int] = filter (Option (42)) {
				_ % 2 == 1;
				}

			assert (even.isDefined);
			assert (odd.isEmpty);
			}

		"support immutable types" in {
			val list = 1 :: 2 :: 3 :: 4 :: 5 :: Nil;
			val p : Int => Boolean = _ < 4;
			val above = filterNot (list) (p);
			val below = filter (list) (p);

			assert (above === List (4, 5));
			assert (below === List (1, 2, 3));
			}

		"support mutable types" in {
			val buffer = Buffer (1, 2, 3, 4, 5);
			val p : Int => Boolean = _ < 4;
			val above = filterNot (buffer) (p);
			val below = filter (buffer) (p);

			assert (above === List (4, 5));
			assert (below === List (1, 2, 3));
			}
		}


	private def filter[M[_], A] (collection : M[A])
		(p : A => Boolean)
		(implicit F : Filterable[M])
		: M[A] =
		F.filter (collection) (p);


	private def filterNot[M[_], A] (collection : M[A])
		(p : A => Boolean)
		(implicit F : Filterable[M])
		: M[A] =
		F.filterNot (collection) (p);
}

