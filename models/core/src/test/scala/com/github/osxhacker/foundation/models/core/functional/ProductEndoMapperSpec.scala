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

import scalaz.{
	:+: => _,
	Coproduct => _,
	Failure => _,
	Success => _,
	_
	}

import shapeless._

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''ProductEndoMapperSpec''' type defines the unit tests which certify
 * [[com.github.osxhacker.foundation.models.core.functional.ProductEndoMapper]] for
 * fitness of purpose.
 *
 * @author osxhacker
 *
 */
final class ProductEndoMapperSpec
	extends ProjectSpec
{
	/// Class Imports


	/// Class Types
	case class Single[T] (val value : T)


	case class Address (street : String, zip : Option[String])


	case class Person (name : Option[String], address : Address)


	object Shroud
		extends ProductEndoMapper
	{
		implicit val caseOptionString
			: Case.Aux[Option[String], Option[String]] =
			at (_ => None);
	}


	object StringLength
		extends ProductEndoMapper
	{
		implicit val caseSingleString
			: Case.Aux[Single[String], Single[Int]] = at {
			s =>
				
			Single (s.value.length);
			}

		implicit val caseString : Case.Aux[String, Int] = at (_.length);
	}
	

	"ProductEndoMapper" must {
		"support 'flat' ADT's" in {
			val singleString = Single ("string");
			val stringAndInt = "two" :: 2 :: HNil;

			val single = StringLength (singleString);
			val mixed = StringLength (stringAndInt);
			
			assert (single.value === singleString.value.length);
			assert (mixed.head === 3);
			assert (mixed.tail.head === 2);
			}

		"support nested ADT's" in {
			val person = Person (
				Some ("bob"),
				Address ("123 Main St.", Some ("90210"))
				);

			val mapped = Shroud (person);

			assert (mapped.name.isEmpty, mapped.toString);
			assert (mapped.address.zip.isEmpty, mapped.address.toString);
			}

		"support Either" in {
			val root : Either[String, String] = Right ("side");

			val mapped = StringLength (root);

			assert (mapped ne null);
			assert (mapped.isRight);
			assert (mapped.exists (_ === 4));
			}

		"support populated Options" in {
			val root : Option[String] = Some ("side");

			val mapped = StringLength (root);

			assert (mapped ne null);
			assert (mapped.isDefined);
			assert (mapped.exists (_ === 4));
			}

		"support empty Options" in {
			val root : Option[String] = None

			val mapped = StringLength (root);

			assert (mapped ne null);
			assert (mapped.isEmpty);
			}

		"support nested Coproducts" in {
			val root : Option[String] \/ String = -\/ (Some ("abc"));

			val mapped = StringLength (root);

			assert (mapped ne null);
			assert (mapped.isLeft);
			assert (mapped.swap.forall (_ === Some (3)));
			}

		"support ADT's within Coproducts" in {
			val root : Option[Person] = Option (
					Person (
					Some ("bob"),
					Address ("123 Main St.", Some ("90210"))
					)
				);

			val mapped = Shroud (root);

			assert (mapped ne null);
			assert (mapped.isDefined);
			assert (mapped.exists (_.name.isEmpty), mapped.toString);
			assert (mapped.exists (_.address.zip.isEmpty), mapped.toString);
			}
		}
}

