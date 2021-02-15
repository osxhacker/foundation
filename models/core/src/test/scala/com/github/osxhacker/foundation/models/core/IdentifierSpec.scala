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

package com.github.osxhacker.foundation.models.core

import scala.language.postfixOps

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import org.scalatest.DiagrammedAssertions
import net.{
	Scheme,
	URN
	}


/**
 * The '''IdentifierSpec''' type defines the unit tests certifying the
 * [[com.github.osxhacker.foundation.models.core.Identifier]] type for fitness of
 * purpose and also serves as an exemplar of how it can be used.
 *
 * @author osxhacker
 */
class IdentifierSpec
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import scalaz.Id._
	import std.option._


	/// Class Types
	case class SomeType ()
	case class AnotherType ()
	
	
	/// Instance Properties
	implicit val scheme1 = Scheme[SomeType] ("some-type");
	implicit val scheme2 = Scheme[AnotherType] ("another-type");

	val goodUrn = new URN (Scheme[Nothing] ("test"), "an-identifier");
	
	
	"An Identifier" when {
		"being created" must {
			"support creation from a URI" in {
				assert (new Identifier (goodUrn).urn !== null);
				}
			
			"support creation from a valid URI String" in {
				assert (Identifier (goodUrn.toString).isRight);
				assert (Identifier (goodUrn.toString).orThrow.urn
					=== goodUrn
					);
				}
			
			"support creation for a specific domain type" in {
				assert (Identifier (scheme1, "1234").isRight);
				}

			"detect a bad URN" in {
				assert (Identifier ("bad URN").isLeft);
				}
			}

		"comparing" should {
			"be disequal with different schemes" in {
				assert (
					Identifier (scheme1, "1") !== Identifier (scheme2, "1")
					);
				}

			"be equal with same scheme and value" in {
				assert (
					Identifier (scheme1, "1").toOption.get
						=== Identifier (scheme1, "1").toOption.get
					);

				assert (
					Identifier (scheme2, "1").toOption.get
						=== Identifier (scheme2, "1").toOption.get
					);
				}
			}

		"determining category" must {
			"know which scheme it belongs to" in {
				val id = Identifier (scheme2, "abcde") orThrow;

				assert (id.belongsTo[AnotherType]);
				}

			"indicate when it does not belong to a scheme" in {
				val id = Identifier (scheme1, "99") orThrow;

				assert (!id.belongsTo[AnotherType]);
				}
			}

		"interoperating" must {
			"be able to be externalized" in {
				val id = Identifier (scheme1, "unique-value") orThrow;
				val externalized = id.externalized[Option] ();
				val expectedPrefix = "urn:%s:".format (scheme1.name);

				assert (externalized.isDefined);
				assert (externalized.exists (_.startsWith (expectedPrefix)));
				}

			"be able to be internalized" in {
				val id = Identifier (scheme1, "unique-value") orThrow;
				val externalized = id.externalized[Id] ();
				val internalized = Identifier (externalized) orThrow;

				assert (internalized === id);
				}

			"be able to be internalized (handling URL special characters)" in {
				val id = Identifier (scheme1, "unique@value") orThrow;
				val externalized = id.externalized[Id] ();
				val internalized = Identifier (externalized) orThrow;

				assert (externalized.contains ('%'));
				assert (internalized === id);
				}
			}
		}
}

