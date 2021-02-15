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

package com.github.osxhacker.foundation.models.core


import java.util.concurrent.Callable

import scala.language._

import scalaz._


/**
 * The '''Scenario''' type embodies a UML Use-Case scenario in code.
 * What this means is that concrete '''Scenario'''s are models of
 * the behaviour defined in Use-Case requirements which, by
 * [[http://en.wikipedia.org/wiki/Use_case this definition]] can be
 * thought of as:
 * 
 * <blockquote>
 * a use case is a list of steps, typically defining interactions between a
 * role (known in UML as an "actor") and a system, to achieve a goal. The actor
 * can be a human or an external system.
 * </blockquote>
 * 
 * A key concept expressed in the aforementioned definition is ''a list of
 * steps''.	Here, we capture this as being a '''Scenario''' which is
 * responsible for defining the activities constituting the Use-Case.
 *
 * @author osxhacker
 *
 */
trait Scenario[+A]
	extends (() => A)
{
	/// Self Type Constraints
	self =>


	/**
	 * Similar to the `map` method, flatMap requires a '''f'''unctor which
	 * consumes an ''A'' and produces a `Scenario[B]` instance, where
	 * ''B'' does not have to be related to ''A'' in any way.
	 */
	def flatMap[B] (f : A => Scenario[B]) : Scenario[B] = f (apply ());


	/**
	 * The foreach method is provided for conveniently using the result of a
	 * '''Scenario''' in an "effectual" way.
	 */
	def foreach[U] (f : A => U) : Unit = f (apply ());


	/**
	 * The map monadic method applies a given '''f'''unctor to the result of
	 * '''this''' '''Scenario''', thus allowing "chaining" of
	 * logic "within" a '''Scenario'''.
	 */
	def map[B] (f : A => B) : Scenario[B] = new Scenario[B] {
		override def apply : B = f (self ());
		}


	/**
	 * The run method is an alias for `apply`.
	 */
	def run () : A = apply ();
	
	
	/**
	 * This version of toString provides a generic description and is intended
	 * to be overridden by concrete types where possible.
	 */
	override def toString () : String = "<scenario>";
}


sealed trait ScenarioStateInstances0
{
	implicit def ScenarioStateFR1[G[_], RA, S] (
		scenario : Scenario[S => RA]
		)
		(implicit M : Monad[G], A0 : Unapply[Apply, RA])
		: IndexedStateT[G, S, S, A0.M[A0.A]] =
		IndexedStateT[G, S, S, A0.M[A0.A]] {
			s => M.point (s -> A0 (scenario.run () (s)));
			}


	implicit def ScenarioStateR1[G[_], RA, S] (
		scenario : Scenario[RA]
		)
		(implicit M : Monad[G], A0 : Unapply[Apply, RA])
		: IndexedStateT[G, S, S, A0.M[A0.A]] =
		IndexedStateT[G, S, S, A0.M[A0.A]] (s => M.point (s -> A0 (scenario ())));
}


sealed trait ScenarioStateInstances
	extends ScenarioStateInstances0
{
	implicit def ScenarioStateR2[G[_], R[_, _], A, B, S] (
		scenario : Scenario[R[A, B]]
		)
		(implicit M : Monad[G])
		: IndexedStateT[G, S, S, R[A, B]] =
		IndexedStateT[G, S, S, R[A, B]] (s => M.point (s -> scenario ()));


	implicit def ScenarioStateFR2[G[_], R[_, _], A, B, S] (
		scenario : Scenario[S => R[A, B]]
		)
		(implicit M : Monad[G])
		: IndexedStateT[G, S, S, R[A, B]] =
		IndexedStateT[G, S, S, R[A, B]] {
			s => M.point (s -> scenario.run () (s));
			}
}


object Scenario
	extends ScenarioStateInstances
{
	/// Class Types
	private class ScalazSupport
		extends Monad[Scenario]
			with Unzip[Scenario]
			with Zip[Scenario]
	{
		override def ap[A, B] (fa : => Scenario[A]) (f : => Scenario[A => B])
			: Scenario[B] = f map (_ (fa ()));


		override def bind[A, B] (s : Scenario[A]) (f : A => Scenario[B])
			: Scenario[B] = s flatMap (f);


		override def point[A] (a : => A) = Scenario (a);


		override def map[A, B] (fa : Scenario[A]) (f : A => B)
			: Scenario[B] = fa map (f);


		override def unzip[A, B] (ab : Scenario[(A, B)])
			: (Scenario[A], Scenario[B]) =
		{
			val (a, b) = ab ();
			
			return (Scenario (a), Scenario (b));
		}
		
		
		override def zip[A, B] (a : => Scenario[A], b : => Scenario[B])
			: Scenario[(A, B)] =
			Scenario (a () -> b ());
	}


	/**
	 * The apply method allows for creation of a '''Scenario'''
	 * from an arbitrary '''expr'''ession in the functional style.
	 */
	def apply[T] (expr : => T) : Scenario[T] = new Scenario[T] {
		override def apply = expr;
		}


	/// Implicit Conversions
	implicit class RichScenario[M[_, _], T, U] (val scenario : Scenario[M[T, U]])
		extends AnyVal
	{
		/**
		 * The andThen method makes composing '''Scenario'''s easier by
		 * supporting syntax such as:
		 * 
		 * {{{
		 * val f1 Throwable \/ Int = ...
		 * val f2 Throwable \/ Int = ...
		 * 
		 * Scenario (f1) andThen (Scenario (f2))
		 * }}}
		 * 
		 * Composition when using containers having only one parameter, such
		 * as `C[_]` can use `flatMap`
		 */
		def andThen[V] (other : U => Scenario[M[T, V]])
			(implicit B : Bind[({ type L[X] = M[T, X] })#L])
			: Scenario[M[T, V]] =
			new Scenario[M[T, V]] {
				override def apply : M[T, V] =
					B.bind (scenario ()) (other (_) ());
				}
	}


	implicit def ScenarioCallable[A] (s : Scenario[A]) : Callable[A] =
		new Callable[A] {
			override def call : A = s ();
			}


	implicit def ScenarioMonad : Monad[Scenario] = new ScalazSupport;


	implicit def ScenarioMonoid[A : Monoid] : Monoid[Scenario[A]] =
		new Monoid[Scenario[A]] {
			override val zero : Scenario[A] = new Scenario[A] {
				override def apply : A = implicitly[Monoid[A]].zero;
				}


			override def append (first : Scenario[A], second : => Scenario[A])
				: Scenario[A] =
				new Scenario[A] {
					override def apply : A =
						implicitly[Monoid[A]].append (first (), second ());
					}
		}


	implicit def ScenarioUnzip : Unzip[Scenario] = new ScalazSupport;


	implicit def ScenarioZip : Zip[Scenario] = new ScalazSupport;
}

