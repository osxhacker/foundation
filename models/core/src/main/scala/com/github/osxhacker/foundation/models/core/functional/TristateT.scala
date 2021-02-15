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

import scala.language.{
	higherKinds,
	postfixOps
	}

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

import com.github.osxhacker.foundation.models.core.error.ApplicationError


/**
 * The '''TristateT''' type defines a
 * [[http://eed3si9n.com/learning-scalaz/Monad+transformers.html Monad transformer]]
 * which abstracts over ''M'' such that '''TristateT''' can represent
 * conditions where ''M'' is "absent."  Since ''M'' must be a model of
 * [[scalaz.Monoid]], "absent" is different than an `mzero` `M[A]`.
 * 
 * Conceptually, '''TristateT''' is analogous to [[scalaz.OptionT]], with the
 * addition of the aforementioned presence knowledge.  As such, the method
 * names chosen reflect those in [[scalaz.OptionT]] and the '''TristateT'''s
 * implementation parallels that found in [[scalaz.OptionT]] as well.
 *
 * @author osxhacker
 *
 */
sealed abstract class TristateT[M[_], A] ()
	extends Product
		with Serializable
{
	/// Class Imports
	import std.option._
	import syntax.monoid._
	import syntax.std.boolean._
	import syntax.std.option._


	/// Instance Properties
	def run : Option[M[A]];


	/**
	 * Alias for `append`.
	 */
	@inline
	final def ++ (other : TristateT[M, A])
		(implicit S : Semigroup[M[A]])
		: TristateT[M, A] =
		append (other);


	/**
	 * Alias for `valueOr`.
	 */
	@inline
	def \/>[E <: ApplicationError] (e : => E) : ErrorOr[M[A]] =
		valueOr (e);


	/**
	 * Alais for `getOrElse`.
	 */
	@inline
	def | (default : => A)
		(implicit A : Applicative[M])
		: M[A] =
		getOrElse (default);


	/**
	 * Alais for `orElse`.
	 */
	@inline
	def ||| (default : => TristateT[M, A]) : TristateT[M, A] =
		orElse (default);


	def ap[B] (f : TristateT[M, A => B])
		(implicit AP : Apply[M])
		: TristateT[M, B];


	def append (other : TristateT[M, A])
		(implicit S : Semigroup[M[A]])
		: TristateT[M, A];


	def exists (p : A => Boolean)
		(implicit F : Foldable[M])
		: Boolean;


	def filter (p : A => Boolean)
		(implicit F : Filterable[M])
		: TristateT[M, A];


	def filterNot (p : A => Boolean)
		(implicit F : Filterable[M])
		: TristateT[M, A]


	def flatMap[B] (f : A => TristateT[M, B])
		(implicit B : Bind[M], PE : PlusEmpty[M])
		: TristateT[M, B]


	def flatMapF[B] (f : A => M[B])
		(implicit B : Bind[M])
		: TristateT[M, B]


	def fold[B] (present : M[A] => B, absent : => B) : B;


	def forall (p : A => Boolean)
		(implicit F : Foldable[M])
		: Boolean;


	def foreach (f : A => Unit)
		(implicit F : Foldable[M])
		: Unit;


	def getOrElse (default : => A)
		(implicit A : Applicative[M])
		: M[A];


	def isAbsent : Boolean;


	def isPresent : Boolean;


	def isZero (implicit MO : Monoid[M[A]], E : Equal[M[A]]) : Boolean;


	def map[B] (f : A => B)
		(implicit F : Functor[M])
		: TristateT[M, B];


	def mapT[N[_], B] (f : M[A] => N[B])
		: TristateT[N, B];


	def orElse (default : => TristateT[M, A]) : TristateT[M, A];


	def orEmpty (implicit PE : PlusEmpty[M]) : M[A];


	def orZero (implicit MO : Monoid[M[A]]) : M[A];


	def traverse[N[_], B] (f : A => N[B])
		(implicit AP : Applicative[N], F : Traverse[M])
		: N[TristateT[M, B]];


	/**
	 * Alias for 'orZero'.
	 */
	final def unary_~ (implicit MO : Monoid[M[A]]) : M[A] = orZero;


	def valueOr[E <: ApplicationError] (e : => E) : ErrorOr[M[A]];
}


object TristateT
	extends TristateTFunctions
		with TristateTInstances
{
	/// Class Types
	/**
	 * The '''Lift''' `object` defines a [[shapeless.Poly1]] which will lift
	 * elements of a [[shapeless.HList]] into the
	 * [[com.github.osxhacker.foundation.models.core.functional.TristateT]] monad
	 * transformer.
	 *
	 * @author osxhacker
	 *
	 */
	object Lift
		extends LiftPriority0
	{
		implicit def default[M[_], A]
			: Case.Aux[TristateT[M, A], TristateT[M, A]] =
			at[TristateT[M, A]] (identity);
	}


	sealed trait LiftPriority0
		extends LiftPriority1
	{
		implicit def container[M[_], A] : Case.Aux[M[A], TristateT[M, A]] =
			at[M[A]] {
				ma =>

				tristateT (Option (ma));
				}
	}


	sealed trait LiftPriority1
		extends Poly1
	{
		implicit def scalar[A]
			: Case.Aux[A, TristateT[Option, A]] =
			at[A] {
				a =>

				presentT (Option (a));
				}
	}


	def apply[M[_], A] (oma : Option[M[A]]) : TristateT[M, A] =
		tristateT (oma);
}


final private case class AbsentT[M[_], A] ()
	extends TristateT[M, A] ()
{
	/// Instance Properties
	override val isAbsent : Boolean = true;
	override val isPresent : Boolean = false;
	override lazy val run : Option[M[A]] = None;


	override def ap[B] (f : TristateT[M, A => B])
		(implicit AP : Apply[M])
		: TristateT[M, B] =
		new AbsentT[M, B] ();


	override def append (other : TristateT[M, A])
		(implicit S : Semigroup[M[A]])
		: TristateT[M, A] =
		other;


	override def exists (p : A => Boolean)
		(implicit F : Foldable[M])
		: Boolean =
		false;


	override def filter (p : A => Boolean)
		(implicit F : Filterable[M])
		: TristateT[M, A] =
		this;


	override def filterNot (p : A => Boolean)
		(implicit F : Filterable[M])
		: TristateT[M, A] =
		this;


	override def flatMap[B] (f : A => TristateT[M, B])
		(implicit B : Bind[M], PE : PlusEmpty[M])
		: TristateT[M, B] =
		new AbsentT[M, B] ();


	override def flatMapF[B] (f : A => M[B])
		(implicit B : Bind[M])
		: TristateT[M, B] =
		new AbsentT[M, B] ();


	override def fold[B] (present : M[A] => B, absent : => B) : B = absent;


	override def forall (p : A => Boolean)
		(implicit F : Foldable[M])
		: Boolean =
		true;


	override def foreach (f : A => Unit)
		(implicit F : Foldable[M])
		: Unit =
		();


	override def getOrElse (default : => A)
		(implicit A : Applicative[M])
		: M[A] =
		A.point (default);


	override def isZero (implicit MO : Monoid[M[A]], E : Equal[M[A]])
		: Boolean =
		false;


	override def map[B] (f : A => B)
		(implicit F : Functor[M])
		: TristateT[M, B] =
		new AbsentT[M, B] ();


	override def mapT[N[_], B] (f : M[A] => N[B])
		: TristateT[N, B] =
		new AbsentT[N, B] ();


	override def orElse (default : => TristateT[M, A]) : TristateT[M, A] =
		default;


	override def orEmpty (implicit PE : PlusEmpty[M]) : M[A] = PE.empty[A];


	override def orZero (implicit MO : Monoid[M[A]]) : M[A] = MO.zero;


	override def traverse[N[_], B] (f : A => N[B])
		(implicit AP : Applicative[N], F : Traverse[M])
		: N[TristateT[M, B]] =
		AP.point (new AbsentT[M, B] ());


	override def valueOr[E <: ApplicationError] (e : => E) : ErrorOr[M[A]] =
		-\/ (e);
}


final private case class PresentT[M[_], A] (private val underlying : M[A])
	extends TristateT[M, A] ()
{
	/// Class Imports
	import std.option._
	import syntax.monoid._
	import syntax.std.boolean._
	import syntax.std.option._


	/// Instance Properties
	override val isAbsent : Boolean = false;
	override val isPresent : Boolean = true;
	override lazy val run : Option[M[A]] = Some (underlying);


	override def ap[B] (f : TristateT[M, A => B])
		(implicit AP : Apply[M])
		: TristateT[M, B] =
		f.run match {
			case None =>
				new AbsentT[M, B] ();

			case Some (mab) =>
				new PresentT[M, B] (AP.ap (underlying) (mab));
			}


	override def append (other : TristateT[M, A])
		(implicit S : Semigroup[M[A]])
		: TristateT[M, A] =
		other.run.fold (this) {
			ma =>

			new PresentT (S.append (underlying, ma));
			}


	override def exists (p : A => Boolean)
		(implicit F : Foldable[M])
		: Boolean =
		F.any (underlying) (p);


	override def filter (p : A => Boolean)
		(implicit F : Filterable[M])
		: TristateT[M, A] =
		new PresentT[M, A] (F.filter (underlying) (p));


	override def filterNot (p : A => Boolean)
		(implicit F : Filterable[M])
		: TristateT[M, A] =
		new PresentT[M, A] (F.filterNot (underlying) (p));


	override def flatMap[B] (f : A => TristateT[M, B])
		(implicit B : Bind[M], PE : PlusEmpty[M])
		: TristateT[M, B] =
		new PresentT[M, B] (B.bind (underlying) (f (_) orEmpty));


	override def flatMapF[B] (f : A => M[B])
		(implicit B : Bind[M])
		: TristateT[M, B] =
		new PresentT[M, B] (B.bind (underlying) (f));


	override def fold[B] (present : M[A] => B, absent : => B) : B =
		present (underlying);


	override def forall (p : A => Boolean)
		(implicit F : Foldable[M])
		: Boolean =
		F.all (underlying) (p);


	override def foreach (f : A => Unit)
		(implicit F : Foldable[M])
		: Unit =
		F.foldLeft (underlying, ()) {
			case (accum, a) =>

			f (a);
			accum;
			}


	override def getOrElse (default : => A)
		(implicit A : Applicative[M])
		: M[A] =
		underlying;


	override def isZero (implicit MO : Monoid[M[A]], E : Equal[M[A]])
		: Boolean =
		MO.isMZero (underlying);


	override def map[B] (f : A => B)
		(implicit F : Functor[M])
		: TristateT[M, B] =
		new PresentT[M, B] (F.map (underlying) (f));


	override def mapT[N[_], B] (f : M[A] => N[B])
		: TristateT[N, B] =
		new PresentT[N, B] (f (underlying));


	override def orElse (default : => TristateT[M, A]) : TristateT[M, A] = this;


	override def orEmpty (implicit PE : PlusEmpty[M]) : M[A] = underlying;


	override def orZero (implicit MO : Monoid[M[A]]) : M[A] = underlying;


	override def traverse[N[_], B] (f : A => N[B])
		(implicit AP : Applicative[N], F : Traverse[M])
		: N[TristateT[M, B]] =
		AP.map (F.traverse (underlying) (f)) {
			PresentT (_);
			}


	override def valueOr[E <: ApplicationError] (e : => E) : ErrorOr[M[A]] =
		\/- (underlying);
}


trait TristateTFunctions
{
	/// Class Imports
	import syntax.std.boolean._


	/// Class Types
	implicit class TristateTToOption[A] (val self : TristateT[Option, A])
	{
		/// Class Imports
		import std.option._
		import syntax.bind._


		@inline
		def toOption () : Option[A] = self.run.join;
	}


	def absentT[M[_], A] () : TristateT[M, A] = AbsentT[M, A] ();


	def emptyT[M[_], A] ()
		(implicit MO : Monoid[M[A]])
		: TristateT[M, A] =
		presentT (MO.zero);


	/**
	 * The mkTristateT method is a model of the FACTORY pattern and will mint
	 * a '''TristateT'''.  If '''p''' is `true`, the '''TristateT''' will be
	 * present (yet may still be empty).
	 */
	def mkTristateT[M[_], A] (p : Boolean)
		(f : => M[A])
		: TristateT[M, A] =
		tristateT (p.option (f));


	def presentT[M[_], A] (ma : M[A]) : TristateT[M, A] =
		PresentT (ma);


	def tristateT[M[_], A] (oma : Option[M[A]]) : TristateT[M, A] =
		oma.fold (absentT[M, A] ()) {
			presentT[M, A]
			}
}


sealed trait TristateTInstances0
{
	/// Implicit Conversions
	implicit def tristateTFunctor[F[_]] (implicit F0 : Functor[F])
		: Functor[({ type L[X] = TristateT[F, X] })#L] =
		new TristateTFunctor[F] {
			override val F = F0;
			}
}


sealed trait TristateTInstances1
	extends TristateTInstances0
{
	/// Implicit Conversions
	implicit def tristateTBind[F[_]] (implicit F0 : Bind[F], F1 : PlusEmpty[F])
		: Bind[({ type L[X] = TristateT[F, X] })#L] =
		new TristateTBind[F] {
			override val B = F0;
			override val F = F0;
			override val PE = F1;
			}
}


sealed trait TristateTInstances2
	extends TristateTInstances1
{
	/// Implicit Conversions
	implicit def tristateTMonad[F[_]] (
		implicit F0 : Monad[F],
		F1 : PlusEmpty[F]
		)
		: Monad[({ type L[X] = TristateT[F, X] })#L] =
		new TristateTMonad[F] {
			override val B = F0;
			override val F = F0;
			override val M = F0;
			override val PE = F1;
			}


	implicit def tristateTSemigroup[F[_], A] (implicit S : Semigroup[F[A]])
		: Semigroup[TristateT[F, A]] =
		new Semigroup[TristateT[F, A]] {
			override def append (a : TristateT[F, A], b : => TristateT[F, A])
				: TristateT[F, A] =
				a ++ b;
			}
}


trait TristateTInstances
	extends TristateTInstances2
{
	/// Instance Properties
	implicit def tristateTEqual[F[_], A] (implicit E : Equal[Option[F[A]]])
		: Equal[TristateT[F, A]] =
		Equal.equalBy (_.run);


	implicit def tristateTMonoid[F[_], A] (implicit S : Semigroup[F[A]])
		: Monoid[TristateT[F, A]] =
		new Monoid[TristateT[F, A]] {
			override val zero : TristateT[F, A] = new AbsentT[F, A] ();


			override def append (a : TristateT[F, A], b : => TristateT[F, A])
				: TristateT[F, A] =
				a ++ b;
			}


	implicit def tristateTPlusEmpty[F[_]]
		: PlusEmpty[({ type L[X] = TristateT[F, X] })#L] =
		new TristateTPlusEmpty[F] {
			}


	implicit def tristateTTraverse[F[_]] (
		implicit F0 : Functor[F],
		F1 : Traverse[F]
		)
		: Traverse[({ type L[X] = TristateT[F, X] })#L] =
		new TristateTTraverse[F] {
			override val F = F0;
			override val T = F1;
			}
}


sealed private trait TristateTBind[F[_]]
	extends Bind[({ type L[X] = TristateT[F, X] })#L]
		with TristateTFunctor[F]
{
	/// Instance Properties
	implicit def B : Bind[F]
	implicit def PE : PlusEmpty[F]


	final override def ap[A, B] (fa : => TristateT[F, A])
		(f : => TristateT[F, A => B])
		: TristateT[F, B] =
		fa ap f;


	final override def bind[A, B] (fa : TristateT[F, A])
		(f : A => TristateT[F, B])
		: TristateT[F, B] =
		fa flatMap f;
}


sealed private trait TristateTFunctor[F[_]]
	extends Functor[({ type L[X] = TristateT[F, X] })#L]
{
	/// Instance Properties
	implicit def F : Functor[F]


	final override def map[A, B] (fa : TristateT[F, A])
		(f : A => B)
		: TristateT[F, B] =
		fa map f;
}


sealed private trait TristateTMonad[F[_]]
	extends Monad[({ type L[X] = TristateT[F, X] })#L]
		with TristateTBind[F]
{
	/// Instance Properties
	implicit def M : Monad[F]


	final override def point[A] (a : => A) : TristateT[F, A] =
		new PresentT[F, A] (M.point (a));
}


sealed private trait TristateTPlusEmpty[F[_]]
	extends PlusEmpty[({ type L[X] = TristateT[F, X] })#L]
{
	override def empty[A] : TristateT[F, A] = new AbsentT[F, A] ();


	override def plus[A] (a : TristateT[F, A], b : => TristateT[F, A])
		: TristateT[F, A] =
		a orElse b;
}


sealed private trait TristateTTraverse[F[_]]
	extends Traverse[({ type L[X] = TristateT[F, X] })#L]
		with TristateTFunctor[F]
{
	/// Instance Properties
	implicit def T : Traverse[F]


	final override def traverseImpl[G[_] : Applicative, A, B] (
		fa : TristateT[F, A]
		)
		(f : A => G[B])
		: G[TristateT[F, B]] =
		fa traverse f;
}

