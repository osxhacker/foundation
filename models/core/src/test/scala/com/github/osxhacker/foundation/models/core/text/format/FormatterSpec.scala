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

import scala.language.higherKinds
import scala.reflect.ClassTag

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

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''FormatterSpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.core.text.format.Formatter]] type class for
 * fitness of purpose.
 *
 * @author osxhacker
 *
 */
final class FormatterSpec
	extends ProjectSpec
{
	/// Class Imports


	/// Class Types
	sealed trait IdentityProtocol
		extends Protocol
	{
		/// Class Imports
		import scalaz.syntax.all._


		override type Repr = String


		override def createValue[A] (value : A) : Value =
			Value (value.toString);
	
	
		override def emit (root : Tree) : Repr =
			root match {
				case Collection (items) =>
					items map (emit) filterNot (_.isEmpty) mkString (",");

				case Node (children) =>
					children.map {
						case (name, value) =>
							
						s"${name.name}=${emit (value)}";
						}.mkString (",");

				case Value (content) =>
					content;

				case _ : Tree =>
					"";
				}
	}
		

	implicit object IdentityProtocol
		extends IdentityProtocol
	{
		
	}


	/**
	 * The '''Flat''' type exercises ADT's consisting of only builtins.
	 */
	case class Flat (title : String, order : Int, active : Boolean)
	
	
	/**
	 * '''Parent''' and '''Child''' represent nested ADT use.
	 */
	case class Parent (name : Symbol, children : List[Child])
	
	case class Child (name : Symbol, age : Int)
	
	
	/**
	 * '''Tree''' and its subtypes exercise coproduct support.
	 */
	sealed trait Tree
	
	case class Node (name : Symbol, children : List[Tree])
		extends Tree
	
	case class Leaf (name : Symbol, value : String)
		extends Tree


	"Formatting built-in types" must {
		behave like supportsBuiltin[Boolean] (true);
		behave like supportsBuiltin[Short] (123.toShort);
		behave like supportsBuiltin[Int] (45678);
		behave like supportsBuiltin[Long] (0L);
		behave like supportsBuiltin[Float] (0.787F);
		behave like supportsBuiltin[Double] (0.234321);
		behave like supportsBuiltin ("this is a test... beep!");
		behave like supportsBuiltin ('aSymbol);
		behave like supportsBuiltin (Option ("inside option"));

		behave like supportsEmptyCollections (List.empty[Int]);
		behave like supportsEmptyCollections (Seq.empty[String]);
		
		behave like supportsCollections ('a' :: 'b' :: 'c' :: Nil);
		behave like supportsCollections (Set (4L, 3L, 2L, 1L));
		}

	"Formatting HList's" must {
		"support empty HList's" in {
			assert (writeObject (HNil).isEmpty)
			}
		
		"support built-in HList's" in {
			assert (
				writeObject (1 :: "two" :: 3.0 :: HNil) ==
					Some ("1,two,3.0")
					);
			}
		
		"support HList's with scala collections" in {
			assert (
				writeObject (1 :: List (2, 3) :: HNil) ==
					Some ("1,2,3")
				);
			}
		
		"support HList's with an ADT" in {
			assert (
				writeObject (
					Flat ("a", 1, true) :: Flat ("b", 2, false) :: HNil
					) ==
					Some (
						"title=a,order=1,active=true,title=b,order=2,active=false"
						)
				);
			}
		}
	
	"Formatting CList's" must {
		"support builtins" in {
			assert (
				writeObject[Int :+: String :+: CNil] (Inl (5)) ==
					Some ("5")
				);

			assert (
				writeObject[Int :+: String :+: CNil] (Inr (Inl ("hello"))) ==
					Some ("hello")
				);
			}
		
		"support flat ADT's" in {
			assert (
				writeObject[String :+: Flat :+: CNil] (
					Inr (Inl (Flat ("right", 1, true)))
					) ==
					Some ("title=right,order=1,active=true")
				);
			}
		}

	"Formatting flat ADT's" must {
		"produce non-empty content" in {
			assert (writeObject (Flat ("test", 1, true)).isDefined);
			}

		"contain fields" in {
			val result = writeObject (Flat ("test", 2, false));
			
			assert (result.isDefined, "unable to format Flat");
			result foreach {
				content =>

				assert (content.contains ("title=test"));
				assert (content.contains ("order=2"));
				assert (content.contains ("active=false"));
				}
			}
		}

	"Formatting ADT hierarchies" must {
		val nested = Parent (
			'bob,
			Child ('mary, 9) ::
			Child ('scott, 5) ::
			Nil
			);

		"produce non-empty content" in {
			assert (writeObject (nested).isDefined);
			}
		
		"produce field content" in {
			val result = writeObject (nested);
			
			assert (result.isDefined, result.toString);
			result foreach {
				content =>

				assert (content.contains ("name=bob"));
				assert (content.contains ("name=mary,age=9"));
				assert (content.contains ("name=scott,age=5"));
				}
			}
		}
	
	"Formatting ADT coproducts" must {
		val tree = Node (
			'root,
			Node (
				'a,
				Leaf ('childA, "leftmost leaf") :: Nil
				) ::
			Node (
				'b,
				Node (
					'c,
					Leaf ('grandchild, "c") :: Nil
					) ::
				Node (
					'd,
					Leaf ('grandchild, "d") :: Nil
					) ::
				Node (
					'e,
					Node (
						'f,
						Leaf ('greatgrandchild, "f") :: Nil
						) ::
						Nil
					) :: Nil
				) ::
			Nil
			);
		
		"produce non-empty content" in {
			assert (writeObject (tree).isDefined);
			}
		
		"contain nested ADT's" in {
			val result = writeObject (tree);
			
			assert (
				result.forall (_.contains ("value=leftmost leaf")),
				result.toString
				);

			assert (
				result.forall (_.contains ("name=grandchild")),
				result.toString
				);

			assert (
				result.forall (_.contains ("value=f")),
				result.toString
				);
			}
		}
	
	
	private def supportsBuiltin[A] (a : A)
		(implicit formatter : Formatter[IdentityProtocol, A], CT : ClassTag[A])
		=
	{
		val typeName = CT.runtimeClass.getName;
		
		s"support ${typeName}s" in {
			assert (writeObject (a).isDefined);
			}
	}
	
	
	private def supportsCollections[A, C[A] <: Traversable[A]] (as : C[A])
		(
			implicit formatter : Formatter[IdentityProtocol, C[A]],
			CTA : ClassTag[A],
			CTC : ClassTag[C[_]]
		)
		=
	{
		val elementTypeName = CTA.runtimeClass.getName;
		val typeName = CTC.runtimeClass.getName;
		
		s"support ${typeName}[${elementTypeName}]'s" in {
			assert (writeObject (as).isDefined);
			assert (!writeObject (as).get.isEmpty);
			
			assert (
				writeObject (as).get ==
					as.map (_.toString).mkString (",")
				);
			}
	}
	
	
	private def supportsEmptyCollections[A, C[A] <: Traversable[A]] (as : C[A])
		(
			implicit formatter : Formatter[IdentityProtocol, C[A]],
			CTA : ClassTag[A],
			CTC : ClassTag[C[_]]
		)
		=
	{
		val elementTypeName = CTA.runtimeClass.getName;
		val typeName = CTC.runtimeClass.getName;
		
		s"support empty ${typeName}[${elementTypeName}]'s" in {
			assert (writeObject (as).isEmpty);
			}
	}


	private def writeObject[A] (a : A)
		(implicit formatter : Formatter[IdentityProtocol, A])
		: Option[String] =
		Option (IdentityProtocol.emit (formatter.write (a))) filter (!_.isEmpty);
}
