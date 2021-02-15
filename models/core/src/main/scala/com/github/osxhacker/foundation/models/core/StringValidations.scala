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

import scala.reflect.ClassTag

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import error._
import scala.util.matching.Regex


/**
 * The '''StringValidations''' type defines common behaviour for validating
 * the contents of '''String'''s originating from a potentially untrusted
 * source.
 *
 * @author osxhacker
 */
trait StringValidations
{
	/// Class Imports
	import Kleisli.kleisli
	import Scalaz._
	import functional.ErrorOr


	/// Class Types
	type ValidationStep[A] = Kleisli[ErrorOr, String, A]


	final class ValidatingFor (val modelName : String)
	{
		def this (ct : ClassTag[_]) =
			this (ct.runtimeClass.getName);
	}


	/**
	 * The andFinally method provides syntactical convenience for producing an
	 * `Error[A]` from the validated `String`.
	 */
	def andFinally[A] (f : String => ErrorOr[A])
		: ValidationStep[A] =
		step (f);


	def between (min : Int, max : Int)
		(implicit V : ValidatingFor)
		: ValidationStep[String] =
		step {
			s =>

			(s.length () >= min && s.length () <= max) either (s) or (
				DomainValueError (
					s"${V.modelName} is not between (${min}, ${max})"
					)
				);
			}


	/**
	 * The fromString method is the entry point for defining blocks of
	 * '''String''' validations.  Its typical use often resembles:
	 * 
	 * {{{
	 * fromString[SomeDomainType] {
	 *    implicit v =>
	 *    
	 *    (notEmpty >==> ...).run (stringToValidate);
	 *    }
	 * }}}
	 */
	def fromString[T <: AnyRef : ClassTag] (
		block : ValidatingFor => ApplicationError \/ T
		)
		: ApplicationError \/ T =
		block (new ValidatingFor (implicitly[ClassTag[T]]));


	/**
	 * This fromString method is the entry point for defining blocks of
	 * '''String''' validations where a Domain Object Model type is not
	 * involved.  Its typical use often resembles:
	 * 
	 * {{{
	 * fromString ('fieldName) {
	 *    implicit v =>
	 *    
	 *    (notEmpty >==> ...).run (stringToValidate);
	 *    }
	 * }}}
	 */
	def fromString (name : Symbol) (
		block : ValidatingFor => ApplicationError \/ String
		)
		: ApplicationError \/ String =
		block (new ValidatingFor (name.name));


	/**
	 * This matching method affirmatively verifies that the `step` has content
	 * which has __at least one__ part that matches the given '''regex'''.
	 */
	def matching (regex : Regex)
		(implicit V : ValidatingFor)
		: ValidationStep[String] =
		step {
			s =>

			regex.findFirstMatchIn (s).map (_.matched) \/> (
				DomainValueError (
					s"${V.modelName} does not match expected format"
					)
				);
			}


	def matching (regex : String, groups : String*)
		(implicit V : ValidatingFor)
		: ValidationStep[String] =
		matching (new Regex (regex, groups : _*));


	/**
	 * This matchingNot method affirmatively verifies that the `step` does not
	 * have content having __at least one__ part that matches the given
	 * '''regex'''.
	 */
	def matchingNot (regex : Regex)
		(implicit V : ValidatingFor)
		: ValidationStep[String] =
		step {
			s =>

			regex.findFirstMatchIn (s).cata (
				_ => DomainValueError (
					s"${V.modelName} does not match expected format"
					).left[String],

				s.right[ApplicationError]
				);
			}


	def notEmpty ()
		(implicit V : ValidatingFor)
		: ValidationStep[String] =
		step {
			s =>

			!(s ?? "").isEmpty either (s) or (
				DomainValueError (s"${V.modelName} is missing or empty")
				);
			}


	def trim () : ValidationStep[String] =
		step (_.trim ().right[ApplicationError]);


	def withinMaxLength (max : Int)
		(implicit V : ValidatingFor)
		: ValidationStep[String] =
		step {
			s =>

			(s.length () <= max) either (s) or (
				DomainValueError (
					s"${V.modelName} exceeds maximum allowable length"
					)
				);
			}


	private def step[A] = Kleisli.apply[ErrorOr, String, A] _;
}

