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
 * The '''QueryParserSpec''' type defines the unit-tests responsible for
 * verifying the [[com.github.osxhacker.foundation.models.core.net.QueryParser]] type
 * for fitness of purpose as well as serve as an exemplar for its use.
 *
 * @author osxhacker
 *
 */
final class QueryParserSpec
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import std.option._
	import syntax.all._


	/// Class Types
	case class TwoStringsQuery (val a : String, val b : String)
	
	
	case class OptionalStringQuery (val a : Option[String])


	case class StringAndOptionalFieldsQuery (
		val a : String,
		val b : Option[Boolean],
		val c : Option[Int]
		)


	"The QueryParser" must {
		"support no query string being present" in {
			val uri = new URI ("example:/");
			val result = QueryParser[TwoStringsQuery] () (uri);

			assert (result.isRight);
			assert (result.exists (_.isEmpty));
			}

		"support an empty query string" in {
			val uri = new URI ("example:/?");
			val result = QueryParser[TwoStringsQuery] () (uri);

			assert (result.isRight);
			assert (result.exists (_.isEmpty));
			}

		"support required string queries" in {
			val uri = new URI ("example:/?a=foo&b=bar");
			val result = QueryParser[TwoStringsQuery] () (uri);

			assert (result.isRight);
			assert (result.exists (_.isDefined));
			assert (result.exists (_.exists (_.head.a === "foo")));
			assert (result.exists (_.exists (_.head.b === "bar")));
			}

		"ignore unknown parameters" in {
			val uri = new URI ("example:/?a=foo&b=bar&ignore+this=true");
			val result = QueryParser[TwoStringsQuery] () (uri);

			assert (result.isRight);
			assert (result.exists (_.isDefined));
			assert (result.exists (_.exists (_.head.a === "foo")));
			assert (result.exists (_.exists (_.head.b === "bar")));
			}

		"detect when a required parameter is missing" in {
			val uri = new URI ("example:/?b=bar");
			val result = QueryParser[TwoStringsQuery] () (uri);

			assert (result.isLeft);
			}

		"support optional query parameters being missing" in {
			val uri = new URI ("example:/?other=thing");
			val result = QueryParser[OptionalStringQuery] () (uri);

			assert (result.isRight);
			assert (result.exists (_.isDefined));
			assert (result.exists (r => (r >>= (_.head.a)).isEmpty));
			}

		"support optional query parameters being present" in {
			val uri = new URI ("example:/?a=foo");
			val result = QueryParser[OptionalStringQuery] () (uri);

			assert (result.isRight);
			assert (result.exists (_.isDefined));
			assert (result.exists (r => (r >>= (_.head.a)).isDefined));
			}

		"support query types other than string" in {
			val uri = new URI ("example:/?a=string+value&b=true&c=42");
			val result = QueryParser[StringAndOptionalFieldsQuery] () (uri);

			assert (result.isRight);
			assert (result.exists (_.isDefined));
			assert (result.exists (_.exists (_.head.a === "string value")));
			assert (result.exists (_.exists (_.head.b === Some (true))));
			assert (result.exists (_.exists (_.head.c === Some (42))));
			}

		"handle invalid values for Boolean" in {
			val uri = new URI ("example:/?a=string+value&b=ja");
			val result = QueryParser[StringAndOptionalFieldsQuery] () (uri);

			assert (result.isLeft);
			}

		"handle invalid values for Int" in {
			val uri = new URI ("example:/?a=string+value&c=ja");
			val result = QueryParser[StringAndOptionalFieldsQuery] () (uri);

			assert (result.isLeft);
			}
		}
}
