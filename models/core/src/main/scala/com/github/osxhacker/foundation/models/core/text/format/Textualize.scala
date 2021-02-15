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

import java.lang.{
	Boolean => JBoolean,
	Double => JDouble,
	Float => JFloat,
	Integer => JInteger,
	Long => JLong
	}

import scala.language.{
	higherKinds,
	implicitConversions,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import scalaz.syntax.Ops

import com.github.osxhacker.foundation.models.core.functional.Tristate


/**
 * The '''Textualize''' trait defines the domain object model contract for
 * producing a text representation from types which support this operation.  As
 * such, it is a model of the TYPE CLASS pattern, with default implementations
 * defined in
 * [[com.github.osxhacker.foundation.models.core.text.format.TextualizeImplicits]].
 *
 * @author osxhacker
 *
 */
trait Textualize[A]
{
	def apply (a : A) : String;
}


object Textualize
	extends TextualizeImplicits
{
	/**
	 * The apply method is a "summoner" which resolves a '''Textualize'''
	 * '''instance''' for the arbitrary type ''A''.
	 */
	def apply[A] ()
		(implicit instance : Textualize[A])
		: Textualize[A] =
		instance;


	/**
	 * The by method provides syntactic convenience for defining a
	 * '''Textualize''' instance.  For example:
	 * 
	 * {{{
	 * case class Foo (x : Int)
	 * object Foo
	 * {
	 * 	implicit val textualizeFoo = Textualize.by[Foo] (_.x.toString ());
	 * }
	 * }}}
	 */
	def by[A] (f : A => String) : Textualize[A] =
		new Textualize[A] {
			override def apply (a : A) : String = f (a);
			}


	/**
	 * The fromToString method provides syntactic convenience for defining a
	 * '''Textualize''' instance for an ''A'' in terms of its `toString`
	 * method.  For example:
	 * 
	 * {{{
	 * case class Foo (x : Int)
	 * object Foo
	 * {
	 * 	implicit val textualizeFoo = Textualize.fromToString[Foo] ();
	 * }
	 * }}}
	 */
	def fromToString[A <: AnyRef] () : Textualize[A] =
		new Textualize[A] {
			override def apply (a : A) : String = a.toString ();
			}


	/**
	 * The via method provides syntactic convenience for defining a
	 * '''Textualize''' instance, where the result of '''f''' may not be a
	 * `String`.  For example, these two invocations are equivalent:
	 *
	 * {{{
	 * case class Foo (x : Int)
	 *
	 * val usingBy : Textualize[Foo] = Textualize.by (_.x.toString ());
	 * val usingVia : Textualize[Foo] = Textualize.via (_.x);
	 * }}}
	 */
	def via[A, B] (f : A => B)
		(implicit tb : Textualize[B])
		: Textualize[A] =
		new Textualize[A] {
			override def apply (a : A) : String = tb (f (a));
			}
}


trait TextualizeImplicits0
{
	/// Implicit Conversions
	implicit def textualizeEither[L, R] (
		implicit tl : Textualize[L],
		tr : Textualize[R]
		)
		: Textualize[Either[L, R]] =
		new Textualize[Either[L, R]] {
			override def apply (a : Either[L, R]) : String =
				a.fold (tl (_), tr (_));
			}


	implicit def textualizeOption[A] (implicit ta : Textualize[A])
		: Textualize[Option[A]] =
		new Textualize[Option[A]] {
			override def apply (a : Option[A]) : String =
				a.fold ("") (ta (_));
			}


	implicit def textualizeScalazEither[L, R] (
		implicit tl : Textualize[L],
		tr : Textualize[R]
		)
		: Textualize[L \/ R] =
		new Textualize[L \/ R] {
			override def apply (a : L \/ R) : String = a.fold (tl (_), tr (_));
			}


	implicit def textualizeTristate[A] (implicit ta : Textualize[A])
		: Textualize[Tristate[A]] =
		new Textualize[Tristate[A]] {
			override def apply (a : Tristate[A]) : String =
				textualizeOption (ta) (a.toOption ());
			}
}


trait TextualizeImplicits
	extends TextualizeImplicits0
{
	/// Implicit Conversions
	implicit val textualizeJBoolean : Textualize[JBoolean] =
		new Textualize[JBoolean] {
			override def apply (a : JBoolean) : String = a.toString ();
		}


	implicit val textualizeJDouble : Textualize[JDouble] =
		new Textualize[JDouble] {
			override def apply (a : JDouble) : String = a.toString ();
		}


	implicit val textualizeJFloat : Textualize[JFloat] =
		new Textualize[JFloat] {
			override def apply (a : JFloat) : String = a.toString ();
		}


	implicit val textualizeJInteger : Textualize[JInteger] =
		new Textualize[JInteger] {
			override def apply (a : JInteger) : String = a.toString ();
		}


	implicit val textualizeJLong : Textualize[JLong] =
		new Textualize[JLong] {
			override def apply (a : JLong) : String = a.toString ();
		}


	implicit val textualizeString : Textualize[String] =
		new Textualize[String] {
			override def apply (a : String) : String = a;
			}


	implicit def textualizeAnyVal[A <: AnyVal] : Textualize[A] =
		new Textualize[A] {
			override def apply (a : A) : String = a.toString ();
			}
}


final class TextualizeOps[A] (override val self : A)
	(implicit private val T : Textualize[A])
	extends Ops[A]
{
	/**
	 * Alias for `toText`.
	 */
	def mkText () : String = toText ();


	/**
	 * Alias for `toTextWithin`.
	 */
	def mkTextWithin[M[_]] ()
		(implicit A : Applicative[M])
		: M[String] =
		toTextWithin[M] ();


	def toText () : String = T (self);


	def toTextWithin[M[_]] ()
		(implicit A : Applicative[M])
		: M[String] =
		A.point (T (self));
}


trait ToTextualizeOps
{
	/// Implicit Conversions
	implicit def toTextualOps[A] (a : A)
		(implicit textualize : Textualize[A])
		: TextualizeOps[A] =
		new TextualizeOps (a);
}

