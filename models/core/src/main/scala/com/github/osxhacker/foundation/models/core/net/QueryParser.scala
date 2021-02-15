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

import scala.util.parsing.combinator._

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import shapeless.{
	syntax => _,
	_
	}

import shapeless.ops.maps._

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	ParsingError
	}


/**
 * The '''QueryParser''' type provides the ability to parse
 * [[com.github.osxhacker.foundation.models.core.net.URI]] query strings which conform to
 * the format of `a-param=some+text&another=99` and can be used with a
 * `Product` type ''A'' such as:
 * 
 * {{{
 * case class SupportedQuery (
 * 	val mandatory : String,
 * 	val optional : Option[Boolean]
 * 	);
 * 
 * val good = new URI ("scheme:/some/path?mandatory=here&optional=true");
 * val missingRequired = new URI ("scheme:/some/path?optional=true");
 * val unsupported = new URI ("scheme:/some/path?something-else=true");
 * 
 * val first = QueryParser[SupportedQuery] () (good);
 * val second = QueryParser[SupportedQuery] () (missingRequired);
 * val third = QueryParser[SupportedQuery] () (unsupported);
 * 
 * assert (first.isRight);
 * assert (second.isLeft);
 * assert (third.isLeft);
 * }}}
 *
 * The supported field types ''A'' can have are any combination of:
 * 
 *   - `Boolean`
 * 
 *   - `Int`
 * 
 *   - `String`
 * 
 *   - `Option[Boolean]`
 * 
 *   - `Option[Int]`
 * 
 *   - `Option[String]`
 * 
 * @author osxhacker
 *
 */
final case class QueryParser[A <: Product] (val charset : String = "UTF-8")
{
	/// Class Imports
	import QueryParser.{
		BackfillAndConvert,
		Parser
		}

	import shapeless.syntax.std.maps._
	import std.option._
	import syntax.all._
	import syntax.std.option._


	/**
	 * The apply method produces an ''A'' type consisting of the key/value
	 * pairs present in the '''uri''' query property.  Note that both a
	 * missing and empty query `String` will result in a `None`.
	 */
	def apply[R <: HList] (uri : URI)
		(
			implicit lgen : LabelledGeneric.Aux[A, R],
			fm : FromMap[R],
			backfill : BackfillAndConvert[A]
		)
		: ApplicationError \/ Option[A :: Map[Symbol, Any] :: HNil] =
		decodeQuery (uri) traverseU {
			content =>

			for {
				params <- Parser (content)
				backfilled <- BackfillAndConvert[A] (params)
				record <- backfilled.toRecord[R] \/> ParsingError (
					s"unsupported query: '${content}'"
					)
				} yield lgen.from (record) :: backfilled :: HNil;
			}


	private def decodeQuery (uri : URI) : Option[String] =
		Option (uri.getQuery)
			.map (q => URLDecoder.decode (q, charset))
			.filterNot (_.isEmpty);
}


object QueryParser
{
	/// Class Imports
	import std.option._
	import syntax.all._
	import syntax.std.boolean._
	import syntax.std.option._


	/// Class Types
	/**
	 * The '''BackfillAndConvert''' type is a model of the TYPE CLASS pattern
	 * and is responsible for providing default `Map` entries for `Option`
	 * fields as well as doing type conversion from `String` to one of the
	 * supported property types documented in
	 * [[com.github.osxhacker.foundation.models.core.net.QueryParser]].
	 * 
	 * @author osxhacker
	 * 
	 */
	sealed trait BackfillAndConvert[A]
	{
		def apply (key : Option[Symbol], values : Map[Symbol, Any])
			: Map[Symbol, Any];
	}


	object BackfillAndConvert
		extends LabelledProductTypeClassCompanion[BackfillAndConvert]
	{
		/// Class Imports
		import \/.fromTryCatchThrowable


		/// Class Types
		case object EmptyProduct
			extends BackfillAndConvert[HNil]
		{
			override def apply (key : Option[Symbol], values : Map[Symbol, Any])
				: Map[Symbol, Any] =
				values;
		}


		case class Product[H, T <: HList] (
			val name : String,
			val head : BackfillAndConvert[H],
			val tail : BackfillAndConvert[T]
			)
			extends BackfillAndConvert[H :: T]
		{
			override def apply (key : Option[Symbol], values : Map[Symbol, Any])
				: Map[Symbol, Any] =
				tail (None, head (Some (Symbol (name)), values));
		}


		object typeClass
			extends LabelledProductTypeClass[BackfillAndConvert]
		{
			override def product[H, T <: HList] (
				name : String,
				head : BackfillAndConvert[H],
				tail : BackfillAndConvert[T]
				)
				: BackfillAndConvert[H :: T] =
				Product[H, T] (name, head, tail);


			override def emptyProduct : BackfillAndConvert[HNil] = EmptyProduct;


			override def project[A, B] (
				instance : => BackfillAndConvert[B],
				to : A => B,
				from : B => A
				)
				: BackfillAndConvert[A] =
				new BackfillAndConvert[A] {
					override def apply (
						key : Option[Symbol],
						values : Map[Symbol, Any]
						)
						: Map[Symbol, Any] =
						instance (key, values);
					}
		}


		/// Instance Properties
		private val handleError : Throwable => ApplicationError =
			_ match {
				case e : ApplicationError =>
					e;

				case e : Throwable =>
					ParsingError ("error raised during parsing", Some (e));
				}


		def apply[A] (parameters : Map[Symbol, Any])
			(implicit backfill : BackfillAndConvert[A])
			: ApplicationError \/ Map[Symbol, Any] =
			handleError <-: fromTryCatchThrowable[Map[Symbol, Any], Throwable] {
				backfill (None, parameters);
				}


		/// Implicit Conversions
		implicit val optionBooleanBackfillAndConvert
			: BackfillAndConvert[Option[Boolean]] =
			new BackfillAndConvert[Option[Boolean]] {
				override def apply (
					name : Option[Symbol],
					values : Map[Symbol, Any]
					)
				: Map[Symbol, Any] =
				name.cata (
					k => values.contains (k).fold (
						values.updated (k, values.get (k).map (convert)),
						values + (k -> None)
						),

					values
					);


				private def convert : Any => Boolean = {
					case s : String =>
						s.toBoolean;

					case b : Boolean =>
						b;
					}
				}

		implicit val requiredBooleanBackfillAndConvert
			: BackfillAndConvert[Boolean] =
			new BackfillAndConvert[Boolean] {
				override def apply (
					name : Option[Symbol],
					values : Map[Symbol, Any]
					)
					: Map[Symbol, Any] =
					name.cata (
						k => values.updated (k, values.get (k) |> convert),
						values
						);


				private def convert : Option[Any] => Boolean = {
					case Some (s : String) =>
						s.toBoolean;

					case Some (b : Boolean) =>
						b;
					}
				}


		implicit val optionIntBackfillAndConvert
			: BackfillAndConvert[Option[Int]] =
			new BackfillAndConvert[Option[Int]] {
				override def apply (
					name : Option[Symbol],
					values : Map[Symbol, Any]
					)
				: Map[Symbol, Any] =
				name.cata (
					k => values.contains (k).fold (
						values.updated (k, values.get (k).map (convert)),
						values + (k -> None)
						),

					values
					);


				private def convert : Any => Int = {
					case s : String =>
						s.toInt;

					case b : Int =>
						b;
					}
				}


		implicit val requiredIntBackfillAndConvert
			: BackfillAndConvert[Int] =
			new BackfillAndConvert[Int] {
				override def apply (
					name : Option[Symbol],
					values : Map[Symbol, Any]
					)
					: Map[Symbol, Any] =
					name.cata (
						k => values.updated (k, values.get (k) |> convert),
						values
						);


				private def convert : Option[Any] => Int = {
					case Some (s : String) =>
						s.toInt;

					case Some (b : Int) =>
						b;
					}
				}


		implicit val optionStringBackfillAndConvert
			: BackfillAndConvert[Option[String]] =
			new BackfillAndConvert[Option[String]] {
				override def apply (
					name : Option[Symbol],
					values : Map[Symbol, Any]
					)
				: Map[Symbol, Any] =
				name.cata (
					k => values.contains (k).fold (
						values.updated (k, values.get (k)),
						values + (k -> None)
						),

					values
					);
				}

		implicit val requiredStringBackfillAndConvert
			: BackfillAndConvert[String] =
			new BackfillAndConvert[String] {
				override def apply (
					name : Option[Symbol],
					values : Map[Symbol, Any]
					)
					: Map[Symbol, Any] =
					values;
				}
	}


	object Parser
		extends RegexParsers
	{
		/// Instance Properties
		private val reserved = Vector[Char] (';', '/', '?', ':', '@', '&', '=');
		private val allowed = elem ("content", ch => !reserved.contains (ch));

		private lazy val fields : Parser[Map[Symbol, String]] =
			repsep (nameValuePair, '&') ^^ {
				_.toMap;
				}

		private lazy val nameValuePair : Parser[(Symbol, String)] =
			name ~ "=" ~ value ^^ {
				case key ~ _ ~ value =>

				(key, value);
				}

		private lazy val name : Parser[Symbol] =
			rep1 (allowed) ^^ {
				symbol =>

				Symbol (symbol.mkString);
				}

		private lazy val value : Parser[String] =
			rep1 (allowed) ^^ {
				_.mkString;
				}


		def apply (query : String)
			: ApplicationError \/ Map[Symbol, String] =
			parseAll (fields, query.trim ()) match {
				case error : NoSuccess =>
					ParsingError (error.msg).left;

				case Success (payload, next) =>
					next.atEnd.fold (
						payload.right,
						ParsingError (s"invalid query format: '${query}'").left
						);
				}
	}
}

