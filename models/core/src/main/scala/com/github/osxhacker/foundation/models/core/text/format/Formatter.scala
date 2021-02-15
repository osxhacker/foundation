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
	Character => JCharacter,
	Double => JDouble,
	Float => JFloat,
	Integer => JInteger,
	Long => JLong,
	Short => JShort
	}

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	:+: => _,
	Coproduct => _,
	Failure => _,
	Success => _,
	_
	}

import shapeless.{
	Id => _,
	_
	}

import shapeless.labelled._

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError
	}


/**
 * The '''Formatter''' trait defines the contract for type classes which
 * both [[com.github.osxhacker.foundation.models.core.text.format.CanRead]] and
 * [[com.github.osxhacker.foundation.models.core.text.format.CanWrite]] arbitrary types
 * in a homogeneous [[com.github.osxhacker.foundation.models.core.text.format.Protocol]].
 *
 * @author osxhacker
 *
 */
trait Formatter[P <: Protocol, -A]
{
	def write (a : A)
		(implicit p : P)
		: p.Tree;
}


sealed trait ObjectFormatter[P <: Protocol, -A]
	extends Formatter[P, A]
{
	override def write (a : A)
		(implicit p : P)
		: p.Node;
}


sealed trait FormatterLowestPriorityImplicits
{
	/// Implicit Conversions
	implicit def cnilFormatter[P <: Protocol] : Formatter[P, CNil] =
		new Formatter[P, CNil] {
			override def write (a : CNil)
				(implicit p : P)
				=
				p.Empty;
		}


	implicit def hnilObjectFormatter[P <: Protocol] : ObjectFormatter[P, HNil] =
		new ObjectFormatter[P, HNil] {
			override def write (a : HNil)
				(implicit p : P)
				=
				p.Node (Nil);
		}


	implicit def coproductFormatter[P <: Protocol, H, T <: Coproduct] (
		implicit hFormatter : Lazy[Formatter[P, H]],
		tFormatter : Formatter[P, T]
		)
		: Formatter[P, H :+: T] =
		new Formatter[P, H :+: T] {
			override def write (clist : H :+: T)
				(implicit p : P)
				=
				clist.eliminate (
					l => hFormatter.value.write (l),
					r => tFormatter.write (r)
					);
			}


	implicit def hlistFormatter[P <: Protocol, H, T <: HList] (
		implicit hFormatter : Lazy[Formatter[P, H]],
		tFormatter : Formatter[P, T]
		)
		: Formatter[P, H :: T] =
		new Formatter[P, H :: T] {
			override def write (hlist : H :: T)
				(implicit p : P)
				=
			{
				val head = hFormatter.value.write (hlist.head);
				val tail = tFormatter.write (hlist.tail);
			
				p.Collection (List (head, tail));
			}
			}
}


sealed trait FormatterLowerPriorityImplicits
	extends FormatterLowestPriorityImplicits
{
	implicit def clistObjectFormatter[P <: Protocol, K <: Symbol, H, T <: Coproduct] (
		implicit witness : Witness.Aux[K],
		hFormatter : Lazy[Formatter[P, H]],
		tFormatter : Formatter[P, T]
		)
		: Formatter[P, FieldType[K, H] :+: T] =
		new Formatter[P, FieldType[K, H] :+: T] {
			private val fieldName = witness.value;

			override def write (clist : FieldType[K, H] :+: T)
				(implicit p : P)
				=
				clist.eliminate (
					l => p.Node (
						List (fieldName -> hFormatter.value.write (l))
						),

					r => tFormatter.write (r)
					);
			}


	implicit def hlistObjectFormatter[P <: Protocol, K <: Symbol, H, T <: HList] (
		implicit witness : Witness.Aux[K],
		hFormatter : Lazy[Formatter[P, H]],
		tFormatter : ObjectFormatter[P, T]
		)
		: ObjectFormatter[P, FieldType[K, H] :: T] =
		new ObjectFormatter[P, FieldType[K, H] :: T] {
			private val fieldName = witness.value;

			override def write (hlist : FieldType[K, H] :: T)
				(implicit p : P)
				=
			{
				val head = hFormatter.value.write (hlist.head);
				val tail = tFormatter.write (hlist.tail);

				p.Node (List (fieldName -> head) ++ tail.children);
			}
			}


	implicit def genericFamilyFormatter[P <: Protocol, A, CL <: Coproduct] (
		implicit generic : LabelledGeneric.Aux[A, CL],
		cFormatter : Lazy[Formatter[P, CL]]
		)
		: Formatter[P, A] =
		new Formatter[P, A] {
			override def write (a : A)
				(implicit p : P)
				=
				cFormatter.value.write (generic.to (a));
			}


	implicit def genericObjectFormatter[P <: Protocol, A, HL <: HList] (
		implicit generic : LabelledGeneric.Aux[A, HL],
		hFormatter : Lazy[ObjectFormatter[P, HL]]
		)
		: ObjectFormatter[P, A] =
		new ObjectFormatter[P, A] {
			override def write (a : A)
				(implicit p : P)
				=
				hFormatter.value.write (generic.to (a));
			}
}


trait FormatterImplicits
	extends FormatterLowerPriorityImplicits
{
	/// Class Imports
	import scalaz.std.option._
	import scalaz.syntax.bind._
	import scalaz.syntax.std.boolean._
	import scalaz.syntax.std.option._


	/// Implicit Conversions
	implicit def eitherFormatter[P <: Protocol, A, B] (
		implicit FA : Formatter[P, A],
		FB : Formatter[P, B]
		)
		: Formatter[P, Either[A, B]] =
		new Formatter[P, Either[A, B]] {
			override def write (eab : Either[A, B])
				(implicit p : P)
				=
				eab.fold (a => FA.write (a), b => FB.write (b));
			}
	
	
	implicit def optionFormatter[P <: Protocol, A] (
		implicit FA : Formatter[P, A]
		)
		: Formatter[P, Option[A]] =
		new Formatter[P, Option[A]] {
			override def write (oa : Option[A])
				(implicit p : P)
				=
				oa.cata (
					a => FA.write (a),
					p.Empty
					);
			}
	
	
	implicit def scalazEitherFormatter[P <: Protocol, A, B] (
		implicit FA : Formatter[P, A],
		FB : Formatter[P, B]
		)
		: Formatter[P, A \/ B] =
		new Formatter[P, A \/ B] {
			override def write (eab : A \/ B)
				(implicit p : P)
				=
				eab.fold (a => FA.write (a), b => FB.write (b));
			}
	
	
	implicit def traversableFormatter[P <: Protocol, A, C[A] <: Traversable[A]] (
		implicit FA : Formatter[P, A]
		)
		: Formatter[P, C[A]] =
		new Formatter[P, C[A]] {
			override def write (as : C[A])
				(implicit p : P)
				=
				p.Collection (as map (a => FA.write (a)) toSeq);
			}
}


object Formatter
	extends FormatterImplicits
{
	/// Class Imports
	import \/.fromTryCatchThrowable
	import scalaz.syntax.writer._
	import scalaz.syntax.bifunctor._
	import scalaz.syntax.std.boolean._
	import scalaz.syntax.std.option._


	/**
	 * The apply method is provided so that a '''Formatter''' instance can be
	 * summoned as needed and have the proper signature.
	 */
	def apply[P <: Protocol, A] () (implicit f : Formatter[P, A])
		: Formatter[P, A] = f;
	
	
	/// Implicit Conversions
	implicit def booleanFormatter[P <: Protocol] : Formatter[P, Boolean] =
		new Formatter[P, Boolean] {
			override def write (a : Boolean)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def byteArrayFormatter[P <: Protocol] : Formatter[P, Array[Byte]] =
		new Formatter[P, Array[Byte]] {
			override def write (bs : Array[Byte])
				(implicit p : P)
				= p.createValue (bs);
		}
	
	
	implicit def charFormatter[P <: Protocol] =
		new Formatter[P, Char] {
			override def write (a : Char)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def characterFormatter[P <: Protocol] =
		new Formatter[P, JCharacter] {
			override def write (a : JCharacter)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def shortFormatter[P <: Protocol] =
		new Formatter[P, Short] {
			override def write (a : Short)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def jshortFormatter[P <: Protocol] =
		new Formatter[P, JShort] {
			override def write (a : JShort)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def intFormatter[P <: Protocol] =
		new Formatter[P, Int] {
			override def write (a : Int)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def jintegerFormatter[P <: Protocol] =
		new Formatter[P, JInteger] {
			override def write (a : JInteger)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def longFormatter[P <: Protocol] =
		new Formatter[P, Long] {
			override def write (a : Long)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def jlongFormatter[P <: Protocol] =
		new Formatter[P, JLong] {
			override def write (a : JLong)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def floatFormatter[P <: Protocol] =
		new Formatter[P, Float] {
			override def write (a : Float)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def jfloatFormatter[P <: Protocol] =
		new Formatter[P, JFloat] {
			override def write (a : JFloat)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def doubleFormatter[P <: Protocol] =
		new Formatter[P, Double] {
			override def write (a : Double)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def jdoubleFormatter[P <: Protocol] =
		new Formatter[P, JDouble] {
			override def write (a : JDouble)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def stringFormatter[P <: Protocol] =
		new Formatter[P, String] {
			override def write (a : String)
				(implicit p : P)
				= p.createValue (a);
			}


	implicit def symbolFormatter[P <: Protocol] =
		new Formatter[P, Symbol] {
			override def write (a : Symbol)
				(implicit p : P)
				= p.createValue (a.name);
			}
}
