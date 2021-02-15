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

import scala.language.postfixOps

import org.scalatest.DiagrammedAssertions
import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import shapeless.{
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''RichURIOpsSpec''' type defines the unit tests for
 * [[com.github.osxhacker.foundation.models.core.net.RichURIOps]] which are not
 * [[com.github.osxhacker.foundation.models.core.net.EndpointParameter]] related.
 *
 * @author osxhacker
 */
final class RichURIOpsSpec
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import uri._


	"The RichURIOps" must {
		"detect empty URI's" in {
			val empty = new URI ("");

			assert (empty.isEmpty);
			}

		"detect non-empty URI's" in {
			val relative = new URI ("a/relative/uri");

			assert (!relative.isEmpty);
			}

		"categorize hierarchical URI's" in {
			val absolute = new URI ("http://example.com");
			val relative = new URI ("a/relative/uri");
			val opaque = new URI ("mailto:test@example.com");

			assert (absolute.isHierarchical);
			assert (relative.isHierarchical);
			assert (!opaque.isHierarchical);
			}
		
		"categorize URI's as Absolute-URI's or not" in {
			val absolute = new URI ("http://example.com");
			val absolutePath = new URI ("/path/with/no/scheme");
			val relative = new URI ("a/relative/uri");
			val opaque = new URI ("mailto:test@example.com");

			assert (absolute.isAbsoluteURI);
			assert (!absolutePath.isAbsoluteURI);
			assert (!relative.isAbsoluteURI);
			assert (!opaque.isAbsoluteURI);
			}

		"determine if two distinct URI's are related" in {
			val a = new URI ("http://example.com/foo");
			val b = new URI ("https://another.example.com/foo");

			assert (a.isAbsoluteURI);
			assert (b.isAbsoluteURI);
			assert (a.isChildOf (b) === false);
			assert (b.isChildOf (a) === false);
			}

		"determine a parental relationship" in {
			val a = new URI ("http://example.com/foo");
			val b = new URI ("http://example.com/foo/bar");

			assert (a.isAbsoluteURI);
			assert (b.isAbsoluteURI);
			assert (b.isChildOf (a) === true);
			assert (a.isParentOf (b) === true);
			}

		"be able to produce path segments" in {
			val parts = new URI ("http://example.com/foo/bar/baz").segments ();

			assert (parts === List ("foo", "bar", "baz"));
			}

		"be able to 'navigate' up" in {
			val base = new URI ("http://example.com/path");
			val uri = (base ^^).normalize ();

			assert (uri.toASCIIString () === "http://example.com/");
			}

		"be able to 'navigate' up (keeping the query)" in {
			val base = new URI ("http://example.com/path") ? ('x -> 1);
			val uri = (base ^^).normalize ();

			assert (uri.toASCIIString () === "http://example.com/?x=1");
			}

		"be able to 'navigate' over" in {
			val base = new URI ("http://example.com/path");
			val uri = (base.^^ / "different").normalize ();

			assert (uri.toASCIIString () === "http://example.com/different");
			}

		"allows queries to be defined as HList pairs" in {
			val base = new URI ("http://example.com/path");
			val uri = base ? (
				('a -> 1) ::
				("b" -> "two") ::
				HNil
				);

			assert (
				uri.toASCIIString () === "http://example.com/path?a=1&b=two"
				);
			}

		"allows queries to be defined as a Map[String, String]" in {
			val base = new URI ("http://example.com/path");
			val uri = base ? Map (
				"a" -> "1",
				"b" -> "two"
				);

			assert (
				uri.toASCIIString () === "http://example.com/path?a=1&b=two"
				);
			}

		"allows queries to be defined as a Map[Symbol, String]" in {
			val base = new URI ("http://example.com/path");
			val uri = base ? Map (
				'a -> "1",
				'b -> "two"
				);

			assert (
				uri.toASCIIString () === "http://example.com/path?a=1&b=two"
				);
			}

		"avoid 'double escaping' queries" in {
			val base = new URI ("http://example.com/path");
			val uri = base ? Map (
				'a -> "1 plus 1",
				'b -> new URI ("/a/path%20with%20spaces")
				);

			val expected = "http://example.com/path?" +
				"a=1%20plus%201&" +
				"b=/a/path%20with%20spaces";

			assert (uri.toASCIIString () === expected);
			}

		"be able to 'prune' URI paths" in {
			val original = new URI ("/a/b/c/d/e");
			val pruned = original.prune ("c");
			val missing = original.prune ("z");
			val empty = new URI ("http://example.com");

			assert (pruned.getPath === "/a/b");
			assert (missing.getPath === original.getPath);
			assert (empty.prune ("a").getPath === empty.getPath);
			}
		}
}

