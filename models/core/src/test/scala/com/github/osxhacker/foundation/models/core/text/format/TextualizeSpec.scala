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

package com.github.osxhacker.foundation.models.core.text.format

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
 * The '''TextualizeSpec''' type defines the unit-tests which certify the
 * [[com.github.osxhacker.foundation.models.core.text.format.Textualize]] concept for
 * fitness of purpose and serve as an exemplar for its use.
 *
 * @author osxhacker
 *
 */
final class TextualizeSpec ()
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import std.list._
	import std.option._
	import textualize._


	/// Class Types
	final case class Custom (x : Int)


	object Custom
	{
		/// Implicit Conversions
		implicit val CustomTextualize : Textualize[Custom] =
			Textualize.via (_.x);
	}


	"Textualize" must {
		"provide default implimentations for built ins" in {
			assert (Textualize[Boolean] ().apply (false) == "false");
			assert (Textualize[Int] ().apply (123) == "123");
			assert (Textualize[Long] ().apply (123L) == "123");
			assert (Textualize[Float] ().apply (1.23F) == "1.23");
			assert (Textualize[Double] ().apply (1.23) == "1.23");
			}

		"provide an identity implementation for 'String'" in {
			assert (Textualize[String] ().apply ("test") == "test");
			}

		"allow for custom definitions" in {
			val instance = Custom (99);

			assert (Textualize[Custom] ().apply (instance) == "99");
			}

		"support Option types" in {
			val instance : Option[Int] = Some (1);
			val empty : Option[Int] = None;

			assert (Textualize[Option[Int]] ().apply (instance) == "1");
			assert (Textualize[Option[Int]] ().apply (empty) == "");
			}

		"support Either types" in {
			val l : Either[Boolean, String] = Left (true);
			val r : Either[Boolean, String] = Right ("x");

			assert (
				Textualize[Either[Boolean, String]] ().apply (l) == "true"
				);

			assert (
				Textualize[Either[Boolean, String]] ().apply (r) == "x"
				);
			}

		"support Scalaz Either types" in {
			val l : Int \/ Double = -\/ (345);
			val r : Int \/ Double = \/- (5.6);

			assert (Textualize[Int \/ Double] ().apply (l) == "345");
			assert (Textualize[Int \/ Double] ().apply (r) == "5.6");
			}

		"not support Throwable types" in {
			assertDoesNotCompile ("""Textualize[Throwable] ()""");
			assertDoesNotCompile (
				"""Textualize[RuntimeException \/ String] ()"""
					);
			}

		"support 'toText' syntax" in {
			val s = "test";
			val i = 42;
			val od : Option[Double] = Some (1.23);

			assert (s.toText () == s);
			assert (i.toText () == i.toString ());
			assert (od.exists (_.toString () == od.toText ()));
			}

		"support 'toTextWithin' syntax" in {
			val custom = Custom (99);

			assert (custom.toTextWithin[List] () == List ("99"));
			assert (99L.toTextWithin[Option] () == Some ("99"));
			}

		"support 'mkText' syntax" in {
			val s = "test";
			val i = 42;
			val od : Option[Double] = Some (1.23);

			assert (s.mkText () == s);
			assert (i.mkText () == i.toString ());
			assert (od.exists (_.toString () == od.mkText ()));
			}

		"support 'mkTextWithin' syntax" in {
			val custom = Custom (99);

			assert (custom.mkTextWithin[List] () == List ("99"));
			assert (99L.mkTextWithin[Option] () == Some ("99"));
			}
		}
}
