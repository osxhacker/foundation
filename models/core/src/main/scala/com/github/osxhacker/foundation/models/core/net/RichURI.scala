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

import java.net.URLDecoder

import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import scalaz.syntax.Ops

import shapeless.{
	Id => _,
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Identifier
import com.github.osxhacker.foundation.models.core.entity.{
	Entity,
	EntityRef
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.text.format.Textualize


/**
 * The '''RichURIOps''' type defines operations which enhance the
 * [[java.net.URI]] class with behaviour used when building/interacting with
 * services using [[java.net.URI]] to identify desired functionality.
 *
 * @author osxhacker
 */
final class RichURIOps (override val self : URI)
	extends Ops[URI]
{
	/// Class Imports
	import URLDecoder.decode
	import std.anyVal._
	import std.int._
	import std.list._
	import std.string._
	import syntax.all._
	import syntax.std.boolean._
	import syntax.std.option._


	/**
	 * This / method appends the '''ref''' to the existing path, if any.
	 */
	def /[A <: Entity[A]] (ref : EntityRef[A]) : URI = / (ref.id);


	/**
	 * The / method appends an '''id''' to the existing path, if any.
	 */
	def / (id : Identifier) : URI = / (id.withoutScheme ());


	/**
	 * The / method appends a '''subpath''' to the existing path, if any.
	 */
	def / (subpath : String) : URI =
		new URI (
			self.getScheme,
			self.getAuthority,
			Option (self.getPath).map (
				_.stripSuffix ("/") + "/" + subpath.stripPrefix ("/")
				) | subpath,
			self.getQuery,
			self.getFragment
			);


	/**
	 * The / method appends the '''subpaths''' to the existing path, if any.
	 */
	def / (subpaths : TraversableOnce[String]) : URI =
		new URI (
			self.getScheme,
			self.getAuthority,
			Option (self.getPath).map (_.stripSuffix ("/")).orZero +
				"/" +
				subpaths.mkString ("/"),
			self.getQuery,
			self.getFragment
			);


	/**
	 * Alias for the `query (parameters, append = false)` method.
	 */
	def ?[A] (parameters : A)
		(implicit encoder : EndpointParameter[A])
		: URI =
		query (parameters, append = false);


	/**
	 * Alias for the `query (parameters, append = true)` method.
	 */
	def ?+[A] (parameters : A)
		(implicit encoder : EndpointParameter[A])
		: URI =
		query (parameters, append = true);


	/**
	 * The ^^ method creates a new ''URI'' which ultimately resolves to the
	 * parent of '''self''' by appending "/..".
	 */
	def ^^ () : URI =
		new URI (
			self.getScheme,
			self.getAuthority,
			Option (self.getPath).map (_.stripSuffix ("/") + "/..") | "/",
			self.getQuery,
			self.getFragment
			);


	/**
	 * The absoluteOrError method will lift '''self''' into the `\/-` type if it
	 * is determined it `isAbsoluteURI` or will produce an `-\/` instance having
	 * the value '''e'''.
	 */
	def absoluteOrError (e : => ApplicationError) : ApplicationError \/ URI =
		isAbsoluteURI.fold (self.right, e.left);


	/**
	 * The isAbsoluteURI method encodes the categorization defined in the
	 * [[https://tools.ietf.org/html/rfc3986#page-27 Absolute URI]] section of
	 * the [[https://tools.ietf.org/html/rfc3986 URI Syntax]] specification.
	 */
	def isAbsoluteURI () : Boolean = self.isAbsolute && isHierarchical;


	/**
	 * The isChildOf method determines if the '''other''' ''URI'' subsumes this
	 * ''URI''.
	 */
	def isChildOf (other : URI) : Boolean = dominates (other, self);


	/**
	 * The isEmpty method determines if the ''URI'' was created with
	 * an empty string or with all components missing.
	 */
	def isEmpty () : Boolean = self.toString.isEmpty;


	/**
	 * The isHierarchical method implements the categorization specified
	 * in the
	 * [[https://docs.oracle.com/javase/8/docs/api/java/net/URI.html URI syntax and components]]
	 * section.
	 */
	def isHierarchical () : Boolean = !self.isOpaque;


	/**
	 * The isParentOf method is `true` when '''self''' has the same scheme,
	 * port, and host as '''other''' as well as a path which includes the
	 * path of '''other'''.
	 */
	def isParentOf (other : URI) : Boolean = dominates (self, other);


	/**
	 * The prune method removes all `segments` in '''self''' at __and__ below
	 * the given '''branch'''.  For example:
	 *
	 * {{{
	 *	val original = new URI ("foo://a/b/c/d/e");
	 *	val pruned = original.prune ("c");
	 *
	 *	assert (pruned.getPath == "foo://a/b");
	 * }}}
	 */
	def prune (branch : String) : URI =
		new URI (
			self.getScheme,
			self.getAuthority,
			Option (self.getPath).map (_ => segments ())
				.filterNot (_.isEmpty)
				.map {
					parts =>

					parts.takeWhile (_ != branch)
						.mkString ("/", "/", "");
					}
				.orZero,

			self.getQuery,
			self.getFragment
			);


	/**
	 * The query method appends each of the `parameters` to the ''URI''.  All
	 * other components of the ''URI'' are untouched.
	 */
	def query[A] (parameters : A, append : Boolean)
		(implicit encoder : EndpointParameter[A])
		: URI =
		new URI (
			self.getScheme,
			self.getAuthority,
			self.getPath,
			buildQuery (
				Option (self.getQuery) filter (_ => append),
				encoder toParameters (parameters)
				),

			self.getFragment
			);


	/**
	 * The segments method produces a `List` of `String`s each of which
	 * represent the content conceptually thought of as "directories"
	 * constituting the path.
	 */
	def segments () : List[String] =
		Option (self.getPath).toList >>= {
			_.split ("/")
				.toList
				.filterNot (_.isEmpty);
			}


	private def bothNullOrEqual[A <: AnyRef] (a : A, b : A) : Boolean =
		((a eq null) && (b eq null)) || ((a ne null) && a.equals (b));


	/**
	 * The buildQuery method ensures a repeatable
	 * [[https://docs.oracle.com/javase/7/docs/api/java/net/URI.html query]]
	 * value is produced.  Note that while '''prior''' is expected to have been
	 * `decode`d (as per the `getQuery` method), '''additional''' may not be so
	 * is `decode`d here.
	 */
	private def buildQuery (
		prior : Option[String],
		additional : Map[String, String]
		)
		: String =
	{
		val extra = additional.iterator
			.map (p => p._1 + "=" + decode (p._2, "UTF-8"))
			.toVector
			.sorted;

		return (prior.toVector ++ extra).mkString ("&");
	}


	private def dominates (a : URI, b : URI) : Boolean =
		bothNullOrEqual (a.getScheme, b.getScheme) &&
		bothNullOrEqual (a.getAuthority, b.getAuthority) &&
		bothNullOrEqual (a.getHost, b.getHost) &&
		(a.getPort === b.getPort) &&
		(a.getPath ne null) &&
		(b.getPath ne null) &&
		b.getPath.startsWith (a.getPath.stripSuffix ("/") + "/");
}


trait RichURIImplicits
{
	/// Implicit Conversions
	implicit val uriEqual : Equal[URI] = Equal.equalA;

	implicit val uriTextualize : Textualize[URI] =
		Textualize.by (_.toASCIIString ());
}


trait ToRichURIOps
{
	/// Implicit Conversions
	implicit def ToRichURI (uri : URI) : RichURIOps = new RichURIOps (uri);
}

