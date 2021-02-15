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

import scala.language.existentials

import shapeless.{
	Id => _,
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''TuplesSpec''' type defines the unit tests which certify the
 * `functional` behaviour provided for `Tuple` types.
 *
 * @author osxhacker
 */
final class TuplesSpec
	extends ProjectSpec
{
	/// Class Imports
	import shapeless.syntax.std.product._
	import tuples._


	/// Class Types
	final case class NameAndAge (name : String, age : Int)


	"Tuples" must {
		"support flattening" in {
			val t = (1, ("two", 3.0));
			val result : (Int, String, Double) = t.flatten;
			val expected = (1, "two", 3.0);

			assert (result === expected);
			}
		
		"support deeply nested flattening" in {
			val nested =
				(
					(
						("grandchild", 1),
						("another", true),
						"third"
					),
					"a",
					"bee",
					"c?"
				);
			
			val result = nested.flatten;
			val expected = (
				"grandchild",
				1,
				"another",
				true,
				"third",
				"a",
				"bee",
				"c?"
				);

			assert (result === expected);
			}
		
		"support conversion to HList's" in {
			val t = (1, 2, 'c', "d");
			val result = t.toHList;

			assert (result === (1 :: 2 :: 'c' :: "d" :: HNil));
			}

		"not affect ADT's" in {
			val t = (
				NameAndAge ("bob", 42),
				true,
				(
					"nested",
					"tuple",
					(
					"nested",
					"adt",
					NameAndAge ("alice", 24)
					)
				)
				);

			val result : (
				NameAndAge,
				Boolean,
				String,
				String,
				String,
				String,
				NameAndAge
				) = t.flatten;

			assert (result._1.name === "bob");
			assert (result._6 === "adt");
			assert (result._7.name === "alice");
			}
	}
}

