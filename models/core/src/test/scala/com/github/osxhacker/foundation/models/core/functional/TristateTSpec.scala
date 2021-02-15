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

import scala.language.higherKinds

import org.scalatest.DiagrammedAssertions
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

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''TristateTSpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.core.functional.TristateT]] monad transformer
 * for fitness of purpose and serves as an exemplar of its use.
 *
 * @author osxhacker
 *
 */
final class TristateTSpec ()
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import std.list._
	import std.option._
	import std.string._
	import std.vector._
	import syntax.monad._
	import syntax.monoid._
	import syntax.std.option._
	import tristatet._


	/// Class Types
	final case class SampleADT (
		val a : Tristate[Int],
		val bs : TristateT[List, String],
		val c : Tristate[String]
		)


	object SampleADT
	{
		/// Class Imports
		import shapeless.ops.hlist
		import tristate.absent
		import tristatet.absentT


		/// Instance Properties
		val empty = SampleADT (
			a = absent[Int] (),
			bs = absentT[List, String] (),
			c = absent[String] ()
			);


		def apply[
			InL <: HList,
			HL <: HList,
			Lifted <: HList,
			Common <: HList,
			Extra <: HList,
			Unaligned <: HList
			] (properties : InL)
			(
				implicit gen : Generic.Aux[SampleADT, HL],
				mapper : hlist.Mapper.Aux[TristateT.Lift.type, InL, Lifted],
				inter : hlist.Intersection.Aux[Lifted, HL, Common],
				diff : hlist.Diff.Aux[HL, Common, Extra],
				prepend : hlist.Prepend.Aux[Common, Extra, Unaligned],
				align : hlist.Align[Unaligned, HL]
			)
			: SampleADT =
			gen.from (
				align (
					prepend (
						inter (mapper (properties)),
						diff (gen.to (empty))
						)
					)
				);
	}


	"The TristateT monad transformer" must {
		"be able to construct an absent instance" in {
			val instance = absentT[Option, String] ();

			assert (instance.isAbsent);
			assert (!instance.isPresent);
			assert (instance.exists (_.isDefinedAt (0)) === false);
			assert (instance.forall (_.isDefinedAt (0)));
			assert (instance.orZero === mzero[Option[String]]);
			}

		"be able to construct a present, but empty, instance" in {
			val instance = presentT (List.empty[Int]);

			assert (!instance.isAbsent);
			assert (instance.isPresent);
			assert (instance.exists (_ > 0) === false);
			assert (instance.forall (_ > 0));
			assert (instance.orEmpty === List.empty[Int]);
			}

		"be able to construct a present instance" in {
			val instance = presentT (List (1));

			assert (!instance.isAbsent);
			assert (instance.isPresent);
			assert (instance.exists (_ > 0));
			assert (instance.forall (_ > 0));
			assert (instance.orZero === List (1));
			}

		"be able to detect what type of instance to make" in {
			val instance = tristateT (Option (List (1)));

			assert (!instance.isAbsent);
			assert (instance.isPresent);
			assert (instance.exists (_ > 0));
			assert (instance.forall (_ > 0));
			assert (instance.orZero === List (1));
			}

		"support Scalaz Apply" in {
			val i = presentT (Option (1));
			val s = presentT (Option ("test"));
			val emptyFirst = absentT[Option, Int] ()
			val emptySecond = absentT[Option, String] ()

			assert (i.tuple (s).exists (_ == (1 -> "test")));
			assert (emptyFirst.tuple (s).isAbsent);
			assert (emptySecond.tuple (s).isAbsent);
			}

		"support Scalaz Bind" in {
			val instance = presentT (List ("test"));
			val asChars = instance >>= {
				s =>

				presentT (s.toUpperCase.toList);
				}

			assert (asChars.isPresent);
			assert (asChars.orZero === List ('T', 'E', 'S', 'T'));
			}

		"support Scalaz Functor" in {
			val instance = presentT (List (42));
			val mapped = instance.fmap (_.toString);

			assert (mapped.exists (_ == "42"));
			}

		"support Scalaz Monoid" in {
			import std.anyVal._

			val ma = Vector (1, 2, 3);
			val instance = presentT (ma);
			val i2 = presentT (Vector (4));
			val i3 = presentT (Vector (5, 6, 7, 8));
			val laws = Monoid[TristateT[Vector, Int]].monoidLaw;

			assert (laws.associative (instance, i2, i3));
			assert (laws.leftIdentity (instance));
			assert (laws.rightIdentity (instance));
			}

		"support Scalaz Traverse" in {
			import syntax.traverse._

			val instance = presentT (
				Vector (
					some (1),
					some (2),
					some (3),
					some (4),
					some (5)
					)
				);

			val hasNone = presentT (
				Vector (
					some (1),
					some (2),
					none[Int],
					some (4),
					some (5)
					)
				);

			val traversed = instance.sequence;
			val traversedWithNone = hasNone.sequence;

			assert (traversed.isDefined);
			assert (traversedWithNone.isEmpty);
			}

		"abide by the Scalaz Monad laws" in {
			import std.anyVal._

			val ma = Vector (1, 2, 3);
			val tos : Int => String = _.toString;
			val instance = presentT (ma);
			val laws = monadLawsFor (instance);

			assert (laws.leftIdentity (ma, presentT[Vector, Int] (_)));
			assert (laws.homomorphism (tos, 99));
			assert (laws.identityAp (instance));
			assert (laws.interchange (presentT (Vector (tos)), 123));
			assert (laws.rightIdentity (instance));
			}

		"support lifting HList instances" in {
			import shapeless.syntax._

			val in = 1 :: List ("two") :: 3.0 :: Tristate.present (4) :: HNil;
			val out = in.map (TristateT.Lift);

			assert (out.head.isPresent);
			assert (out.head.exists (_ === 1));
			assert (out.last.exists (_ === 4));
			}

		"support complete property construction techniques" in {
			val complete = 1 :: List ("two", "three") :: "four" :: HNil;
			val reordered = List ("two", "three") :: "four" :: 1 :: HNil;
			val instance = SampleADT (complete);

			assert (instance ne null);
			assert (instance.a.exists (_ === 1));
			assert (instance.bs.exists (_ === "two"));
			assert (instance.bs.exists (_ === "three"));
			assert (instance.c.exists (_ === "four"));

			val other = SampleADT (reordered);

			assert (other === instance);
			}

		"support partial property construction techniques" in {
			val partial = List ("two", "three") :: 1 :: HNil;
			val instance = SampleADT (partial);

			assert (instance ne null);
			assert (instance.a.exists (_ === 1));
			assert (instance.bs.exists (_ === "two"));
			assert (instance.bs.exists (_ === "three"));
			assert (instance.c.isAbsent);
			}

		"support toOption when 'M' is an 'Option'" in {
			val property = TristateT.presentT (Option (1));
			val asOption = property.toOption ();

			assert (asOption.isDefined);
			assert (asOption.exists (_ === 1));
			}
		}


	def monadLawsFor[M[_], A] (a : TristateT[M, A])
		(implicit M : Monad[({ type L[X] = TristateT[M, X] })#L])
		=
		M.monadLaw;
}


