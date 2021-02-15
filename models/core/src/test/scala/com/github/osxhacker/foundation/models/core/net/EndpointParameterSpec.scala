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

package com.github.osxhacker.foundation.models.core.net

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import shapeless.{
	Id => _,
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''EndpointParameterSpec''' type defines the unit tests for
 * [[com.github.osxhacker.foundation.models.core.net.EndpointParameter]]
 * [[http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html type class]]
 * behaviour and serves as an exemplar for their use.
 *
 * @author osxhacker
 */
final class EndpointParameterSpec
	extends ProjectSpec
{
	/// Class Imports
	import endpoint._


	/// Class Types
	/**
	 * The '''CustomParameters''' type defines its own, custom,
	 * '''EndpointParameter'''.
	 */
	case class CustomParameters (val as : Any *)


	object CustomParameters
	{
		/// Implicit Conversions
		implicit val CustomEndpointParameter
			: EndpointParameter[CustomParameters] =
			new EndpointParameter[CustomParameters] {
				override def pairs (a : CustomParameters)
					: Traversable[(String, String)] =
					a.as.zipWithIndex.map {
						case (x, index) =>

						(s"a[${index}]", x.toString);
						}
				}
	}


	/**
	 * The '''DerivedParameters''' type has its '''EndpointParameter''' type
	 * class provided through implicit definition.
	 */
	case class DerivedParameters[A] (
		val n : A,
		val flag : Option[Boolean] = Option (false)
		)


	"An EndpointParameter" must {
		"be implicitly available" in {
			assertCompiles (
				"""implicitly[EndpointParameter[DerivedParameters[Int]]] ne null"""
				);
			}

		"produce a map of its properties" in {
			val x = DerivedParameters ("a");

			assert (x.toParameters ne null);
			assert (x.toParameters.contains ("n"));
			}

		"support pairs by using their values" in {
			val pair = "x" -> 99;

			assert (pair.toParameters ne null);
			assert (pair.toParameters () === Map (pair._1 -> pair._2.toString));
			}

		"support pairs having optional values when present" in {
			val pair = "a" -> Option (true);

			assert (pair.toParameters ne null);
			assert (pair.toParameters () === Map ("a" -> "true"));
			}

		"support pairs having optional values when not present" in {
			val pair : (Symbol, Option[Int]) = 'x -> None;

			assert (pair.toParameters ne null);
			assert (pair.toParameters ().isEmpty);
			}

		"support multiple properties from a type" in {
			val three = CustomParameters (1, 2, 3);

			assert (three.toParameters ne null);

			(1 to 3).map (_.toString) foreach {
				expected =>

				assert (three.toParameters.values.exists (_ === expected));
				}
			}

		"use the *last* pair given with the same key" in {
			val first = Map ("n" -> 0);
			val whatWillBeUsed = DerivedParameters (99, Option (true));
			val mixed = first :: whatWillBeUsed :: HNil;

			assert (mixed.toParameters === Map ("n" -> "99", "flag" -> "true"));
			}
		}

	"An EndpointParameter with RichURI" should {
		import uri._

		"be usable as a query parameter" in {
			val x = DerivedParameters ("x");
			val withoutQuery = new URI ("http://example.com");
			val withQuery = withoutQuery ? (x :: ("y" -> 2) :: HNil);

			assert (withoutQuery.getRawQuery eq null);
			assert (withQuery.toString == "http://example.com?flag=false&n=x&y=2");
			}

		"add to an existing query" in {
			val withQuery = new URI ("http://example.com?existing=true");
			val a = CustomParameters (1, 2, 3, 4);
			val additional = withQuery ?+ (a :: HNil);

			assert (
				additional.toString ==
				"http://example.com?existing=true&a[0]=1&a[1]=2&a[2]=3&a[3]=4"
				);
			}
		}
}
