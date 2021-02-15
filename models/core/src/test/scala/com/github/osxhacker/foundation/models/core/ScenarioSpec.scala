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

import scalaz._

import error._


/**
 * The '''ScenarioSpec''' type defines the unit tests certifying the
 * [[com.github.osxhacker.foundation.models.core.Scenario]] type for fitness of
 * purpose.  It also serves as an exemplar of expected behaviour under
 * reasonable use.
 *
 * @author osxhacker
 */
class ScenarioSpec
	extends ProjectSpec
{
	/// Class Imports
	import syntax.either._
	import syntax.std.boolean._
	
	
	/// Class Types
	case class Person (val name : String, age : Int)
	

	case class BirthdayScenario (
		private val context : Person
		)
		extends Scenario[Person]
	{
		override def apply () : Person = context.copy (age = context.age + 1);
			
			
		override def toString = "BirthdayScenario example";
	}
	

	case class MakeYoungerScenario (
		private val context : Person
		)
		extends Scenario[ApplicationError \/ Person]
	{
		override def apply () : ApplicationError \/ Person =
			(context.age > 1) either (context.copy (age = context.age - 1)) or (
				LogicError ("cannot go younger than 1 years old")
				);
	}

		
	case class RenameScenario (
		private val context : Person,
		private val newName : String
		)
		extends Scenario[ApplicationError \/ Person]
	{
		override def apply () : ApplicationError \/ Person =
			(!newName.isEmpty) either (context.copy (name = newName)) or (
				LogicError ("bad test")
				);
			
			
		override def toString = "RenameScenario example";
	}
	
	
	/// Instance Properties
	
	"A Scenario" must {
		"be a model of Monad" in {
			assertCompiles ("implicitly[Monad[Scenario]]");
			}
		
		"be a model of Monoid" in {
			assertCompiles ("import Scalaz._; implicitly[Monoid[Scenario[Int]]]");
			}
		
		"be a model of Unzip" in {
			assertCompiles ("implicitly[Unzip[Scenario]]");
			}
		
		"be a model of Zip" in {
			assertCompiles ("implicitly[Zip[Scenario]]");
			}
		
		"support anonymous creation" in {
			assert (Scenario (42) () === 42);
			}
		}
	
	it should {
		import syntax.all._

		"assist in composing Use-Cases" in {
			val bob = Person ("Bob", 50);
			val composed = RenameScenario (bob, "Alice") andThen (
				MakeYoungerScenario (_)
				);
			val result : ApplicationError \/ Person = composed ();
			
			assert (result.isRight);
			assert (result.valueOr (null) === Person ("Alice", 49));
			}
		
		"be usable with EitherT" in {
			import EitherT._
			
			val mary = Person ("Mary", 40);
			val composed = eitherT (RenameScenario (mary, "Jill")) map (
				BirthdayScenario (_) ()
				);
			val result : ApplicationError \/ Person = composed.run.apply ();
			
			assert (result.isRight);
			assert (result.valueOr (null) === Person ("Jill", 41));
			}
		
		"be able to easily detect problems" in {
			val original = Person ("Hello", 0);
			val stopsEarly = RenameScenario (original, "") >>= {
				_.traverse (_ => Scenario (throw new Exception ()))
				}
			
			assert (stopsEarly ().isLeft);
			}
		
		"be usable with StateT" in {
			import State._
			
			val initial = Person ("test", 50);
			val steps = for {
				_ <- init[Person]
				
				/// Example of "lifting" a scenario
				p <- gets (BirthdayScenario (_ : Person).run)
				
				/// Typical use of a scenario is when they are defined to
				/// return an Either type.
				y <- MakeYoungerScenario (p)
				
				/// Scenarios can be used with StateT when defined inline and
				/// contain compatible functors, or either.
				r <- Scenario ((p : Person) => p.copy (name = "Bob!").right[ApplicationError])
				} yield r;
				
			assert (steps.eval (initial).isRight);
			assert (steps.eval (initial).valueOr (null).age === initial.age);
			assert (steps.eval (initial).valueOr (null).name !== initial.name);
			}
		}
}

