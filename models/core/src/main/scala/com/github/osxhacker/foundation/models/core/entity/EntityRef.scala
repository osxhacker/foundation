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
package entity

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import error.ApplicationError
import net.{
	Scheme,
	URN
	}


/**
 * The '''EntityRef''' type is a Domain Object Model abstraction which reifies
 * a reference to an [[com.github.osxhacker.foundation.models.core.Entity]] instance.
 *
 * @author osxhacker
 *
 */
final case class EntityRef[A <: Entity[A]] (val id : Identifier)
{
	/**
	 * The externalized method produces a `M[URN]` representation of the
	 * '''EntityRef''' capable of being saved and then rehydrated at some point
	 * in the future.
	 */
	def externalized[M[_]] (implicit A : Applicative[M]) : M[URN] =
		A.point (id.urn);
}


object EntityRef
{
	/// Class Imports
	import Scalaz._


	/**
	 * This apply method is provided to allow functional-style creation of an
	 * '''EntityRef''' when the
	 * [[com.github.osxhacker.foundation.models.core.net.Scheme]] is known, but the
	 * [[com.github.osxhacker.foundation.models.core.net.Scheme]]-specific portion of an
	 * [[com.github.osxhacker.foundation.models.core.Identifier]] is from a run-time source
	 * (such as being received from an external agent).
	 * 
	 * For situations where the '''value''' is from an untrusted source, use
	 * the `unapply` extractor method below.
	 */
	def apply[T <: Entity[T] : Scheme] (value : String)
		: ApplicationError \/ EntityRef[T] =
		Identifier (implicitly[Scheme[T]], value) map {
			id => new EntityRef[T] (id);
			}
	
	
	/// Implicit Conversions
	implicit def EntityRefShow[A <: Entity[A]] : Show[EntityRef[A]] =
		new Show[EntityRef[A]] {
			override def shows (a : EntityRef[A]) : String = s"ref=${a.id.urn}";
			}
	
	
	implicit def EntityRefEqual[A <: Entity[A]] : Equal[EntityRef[A]] =
		new Equal[EntityRef[A]] {
			override def equal (a : EntityRef[A], b : EntityRef[A]) =
				a.id === b.id;
			}
}

