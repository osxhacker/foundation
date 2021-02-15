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
	syntax => _,
	_
	}


/**
 * The '''EndpointParameter''' type defines a
 * [[http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html type class]]
 * for arbitrary `A` types to contribute one or more relevant properties
 * to the formulation of a `URI`-based request.
 *
 * @author osxhacker
 */
trait EndpointParameter[A]
{
	/// Class Imports
	import std.string._
	import std.tuple._
	import syntax.functor._
	
	
	/**
	 * The pairs method is expected to map an `A` instance into zero or more
	 * `(String, String)` tuples.  The implementor is ''not'' expected to
	 * have encoded the value(s).
	 */
	def pairs (a : A) : Traversable[(String, String)];
	
	
	/**
	 * The toParameters produces a `Map` of name-value pairs
	 * suitable for inclusion in a
	 * [[com.github.osxhacker.foundation.models.core.net.URI]].
	 */
	final def toParameters (a : A) : Map[String, String] =
		pairs (a) toMap;
}


object EndpointParameter
	extends EndpointParameterImplicits
{
	@inline
	def apply[A] ()
		(implicit EP : EndpointParameter[A])
		: EndpointParameter[A] = EP;
	
	
	def by[A] (name : String, f : A => String)
		: EndpointParameter[A] =
		new EndpointParameter[A] {
			override def pairs (a : A) : Traversable[(String, String)] =
				List (name -> f (a));
			}
}


sealed trait EndpointParameterEncoder
{
	protected def createParameterEncoder[A] (
		f : A => Traversable[(String, String)]
		)
		: EndpointParameter[A] =
		new EndpointParameter[A] {
			override def pairs (a : A) : Traversable[(String, String)] = f (a);
			}
}


sealed trait EndpointParameterProductImplicits
	extends EndpointParameterEncoder
{
	/// Class Imports
	import shapeless.ops.product._


	/// Implicit Conversions
	implicit def productParameters[A <: Product] (
		implicit tm : ToMap[A]
		)
		: EndpointParameter[A] =
		createParameterEncoder[A] {
			a =>

			tm (a).map {
				case (_, _ : None.type) =>
					None;

				case (Symbol (key), Some (value)) =>
					Some (key -> value.toString ());

				case (Symbol (key), value) =>
					Some (key -> value.toString ());
				}
				.filter (_.isDefined)
				.map (_.get);
			}
}


sealed trait EndpointParameterSpecializedImplicits
	extends EndpointParameterProductImplicits
{
	/// Implicit Conversions
	implicit def mapStringAParameter[A]
		: EndpointParameter[Map[String, A]] =
		createParameterEncoder[Map[String, A]] {
			_.map (p => p._1 -> p._2.toString ());
			}


	implicit def mapSymbolAParameter[A]
		: EndpointParameter[Map[Symbol, A]] =
		createParameterEncoder[Map[Symbol, A]] {
			_.map (p => p._1.name -> p._2.toString ());
			}


	implicit def stringPairParameter[A] : EndpointParameter[(String, A)] =
		createParameterEncoder[(String, A)] (p => (p._1 -> p._2.toString () :: Nil));


	implicit def symbolPairParameter[A] : EndpointParameter[(Symbol, A)] =
		createParameterEncoder[(Symbol, A)] (p => (p._1.name -> p._2.toString () :: Nil));
}


trait EndpointParameterImplicits
	extends EndpointParameterSpecializedImplicits
{
	/// Implicit Conversions
	implicit def mapStringStringParameter
		: EndpointParameter[Map[String, String]] =
		createParameterEncoder[Map[String, String]] (m => m);


	implicit def mapSymbolStringParameter
		: EndpointParameter[Map[Symbol, String]] =
		createParameterEncoder[Map[Symbol, String]] {
			_.map (p => p._1.name -> p._2);
			}


	implicit def optionParameter[A] (implicit ep : EndpointParameter[A])
		: EndpointParameter[Option[A]] =
		createParameterEncoder[Option[A]] {
			_.map (ep.pairs).getOrElse (Nil);
			}


	implicit def stringOptionParameter[A]
		: EndpointParameter[(String, Option[A])] =
		createParameterEncoder[(String, Option[A])] {
			p =>

			p._2.map (v => p._1 -> v.toString () :: Nil).getOrElse (Nil);
			}


	implicit def symbolOptionParameter[A]
		: EndpointParameter[(Symbol, Option[A])] =
		createParameterEncoder[(Symbol, Option[A])] {
			p =>

			p._2.map (v => p._1.name -> v.toString () :: Nil).getOrElse (Nil);
			}


	implicit val hnilParameter : EndpointParameter[HNil] =
		createParameterEncoder[HNil] (_ => Nil);


	implicit def hlistParameters[H, T <: HList] (
		implicit headEP : EndpointParameter[H],
		tailEP : EndpointParameter[T]
		)
		: EndpointParameter[H :: T] =
		createParameterEncoder[H :: T] {
			case (h :: t) =>

			headEP.pairs (h) ++ tailEP.pairs (t);
			}
}


final class EndpointParameterOps[A] (override val self : A)
	(implicit EP : EndpointParameter[A])
	extends Ops[A]
{
	def toParameters () : Map[String, String] = EP.toParameters (self);
}


trait ToEndpointParameterOps
{
	/// Implicit Conversions
	implicit def ToEndpointParameter[A] (a : A)
		(implicit EP : EndpointParameter[A]) =
		new EndpointParameterOps[A] (a);
}

