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
	\/,
	-\/,
	\/-
	}

import shapeless._
import poly._


/**
 * The '''ProductEndoMapper''' trait defines a `shapeless.Poly1` which
 * provides `scala.Product` tree navigation and producing the same
 * structure.  Which is why it's named 'Endo', to give a hint as to its
 * [[https://en.wikipedia.org/wiki/Endomorphism endomorphism]] nature.
 *
 * @author osxhacker
 *
 */
trait ProductEndoMapper
	extends ProductEndoMapperPriority0
{
	/// Implicit Conversions
	implicit def caseEither[A, B, C, D] (
		implicit cac : Case.Aux[A, C],
		cbd : Case.Aux[B, D]
		)
		: Case.Aux[Either[A, B], Either[C, D]] =
		at[Either[A, B]] (
			_.fold (
				a => Left (cac.value (a :: HNil)),
				b => Right (cbd.value (b :: HNil))
				)
			);

	implicit def caseScalzEither[A, B, C, D] (
		implicit cac : Case.Aux[A, C],
		cbd : Case.Aux[B, D]
		)
		: Case.Aux[A \/ B, C \/ D] =
		at[A \/ B] (
			_.fold (
				a => -\/ (cac.value (a :: HNil)),
				b => \/- (cbd.value (b :: HNil))
				)
			);


	implicit def caseOption[A, B] (implicit cab : Case.Aux[A, B])
		: Case.Aux[Option[A], Option[B]] =
		at[Option[A]] (_ map (a => cab.value (a :: HNil)));
}


sealed trait ProductEndoMapperPriority0
	extends ProductEndoMapperPriority1
{
	/// Implicit Conversions
	implicit def caseProduct[A, InL <: HList, OutL <: HList] (
		implicit genericIn : Generic.Aux[A, InL],
		genericOut : Generic.Aux[A, OutL],
		mapper : ops.hlist.Mapper.Aux[this.type, InL, OutL]
		)
		: Case.Aux[A, A] =
		at (t => genericOut.from (mapper (genericIn.to (t))));


	implicit def caseCoproduct[A, InC <: Coproduct, OutC <: Coproduct] (
		implicit genericIn : Generic.Aux[A, InC],
		genericOut : Generic.Aux[A, OutC],
		mapper : ops.coproduct.Mapper.Aux[this.type, InC, OutC]
		)
		: Case.Aux[A, A] =
		at (t => genericOut.from (mapper (genericIn.to (t))));
}


sealed trait ProductEndoMapperPriority1
	extends PartialPoly1
{
	/// Implicit Conversions
	implicit val caseCNil : Case.Aux[CNil, CNil] = at (identity);


	implicit def caseHList[InL <: HList, OutL <: HList] (
		implicit mapper : ops.hlist.Mapper.Aux[this.type, InL, OutL]
		)
		: Case.Aux[InL, OutL] =
		at (hl => mapper (hl));
}

