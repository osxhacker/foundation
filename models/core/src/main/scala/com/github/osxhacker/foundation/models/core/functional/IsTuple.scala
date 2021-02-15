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

import shapeless._


/**
 * The '''IsTuple''' type defines a witness that a type ''T'' is one of the
 * built-in Scala `Tuple` types.  By witnessing this, '''IsTuple'''
 * intrinsically provides an isomorphism between the `Tuple` and an `HList`
 * of the same arity and element types.
 *
 * @author osxhacker
 */
sealed trait IsTuple[T]
	extends Serializable
{
	/// Class Types
	type Repr <: HList


	/**
	 * The from method satisfies the same contract as the [[shapeless.Generic]]
	 * `from` method in that a `Tuple` of type ''T'' is produced from the
	 * given ''Repr'' [[shapeless.HList]].
	 */
	def from (list : Repr) : T;


	/**
	 * The to method satisfies the same contract as the [[shapeless.Generic]]
	 * `to` method in that a ''Repr'' non-empty [[shapeless.HList]] is 
	 * produced using the '''tuple''' instance given.
	 */
	def to (tuple : T) : Repr;
}


object IsTuple
{
	/// Class Types
	type Aux[T, HL <: HList] = IsTuple[T] {
		type Repr = HL
		}


	/**
	 * The apply method defines a "summoner" which resolves the '''witness'''
	 * for the `Tuple` type ''T''.
	 */
	def apply[T] ()
		(implicit witness : IsTuple[T])
		: Aux[T, witness.Repr] =
		witness;


	private def createInstance[T, HL <: HList] ()
		(implicit gen : Generic.Aux[T, HL])
		: Aux[T, HL] =
		new IsTuple[T] {
			type Repr = HL

			override def from (list : Repr) : T = gen.from (list);
			override def to (tuple : T) : Repr = gen.to (tuple);
			}


	/// Implicit Conversions
	implicit def tuple1[A] =
		createInstance[Tuple1[A], A :: HNil] ()


	implicit def tuple2[A, B] =
		createInstance[(A, B), A :: B :: HNil] ();


	implicit def tuple3[A, B, C] =
		createInstance[(A, B, C), A :: B :: C :: HNil] ();


	implicit def tuple4[A, B, C, D] =
		createInstance[(A, B, C, D), A :: B :: C :: D :: HNil] ();


	implicit def tuple5[A, B, C, D, E] =
		createInstance[(A, B, C, D, E), A :: B :: C :: D :: E :: HNil] ();


	implicit def tuple6[A, B, C, D, E, F] =
		createInstance[
			(A, B, C, D, E, F),
			A :: B :: C :: D :: E :: F :: HNil
			] ();


	implicit def tuple7[A, B, C, D, E, F, G] =
		createInstance[
			(A, B, C, D, E, F, G),
			A :: B :: C :: D :: E :: F :: G :: HNil
			] ();


	implicit def tuple8[A, B, C, D, E, F, G, H] =
		createInstance[
			(A, B, C, D, E, F, G, H),
			A :: B :: C :: D :: E :: F :: G :: H :: HNil
			] ();


	implicit def tuple9[A, B, C, D, E, F, G, H, I] =
		createInstance[
			(A, B, C, D, E, F, G, H, I),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: HNil
			] ();


	implicit def tuple10[A, B, C, D, E, F, G, H, I, J] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: HNil
			] ();


	implicit def tuple11[A, B, C, D, E, F, G, H, I, J, K] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: HNil
			] ();


	implicit def tuple12[A, B, C, D, E, F, G, H, I, J, K, L] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: HNil
			] ();


	implicit def tuple13[A, B, C, D, E, F, G, H, I, J, K, L, M] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M
				:: HNil
			] ();


	implicit def tuple14[A, B, C, D, E, F, G, H, I, J, K, L, M, N] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: HNil
			] ();


	implicit def tuple15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: HNil
			] ();


	implicit def tuple16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: P :: HNil
			] ();


	implicit def tuple17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: P :: Q :: HNil
			] ();


	implicit def tuple18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: P :: Q :: R :: HNil
			] ();


	implicit def tuple19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: P :: Q :: R :: S :: HNil
			] ();


	implicit def tuple20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: P :: Q :: R :: S :: T :: HNil
			] ();


	implicit def tuple21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: P :: Q :: R :: S :: T :: U :: HNil
			] ();


	implicit def tuple22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V] =
		createInstance[
			(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V),
			A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: K :: L :: M ::
				N :: O :: P :: Q :: R :: S :: T :: U :: V :: HNil
			] ();
}

