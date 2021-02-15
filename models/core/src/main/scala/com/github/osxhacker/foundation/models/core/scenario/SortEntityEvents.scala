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

package com.github.osxhacker.foundation.models.core.scenario

import scala.collection.Seq
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

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.entity.{
	Entity,
	EntityEvent
	}

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError,
	LogicError
	}

import com.github.osxhacker.foundation.models.core.time


/**
 * The '''SortEntityEvents''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] of being able to sort a
 * collection of [[com.github.osxhacker.foundation.models.core.entity.EntityEvent]]s into
 * ascending order based on `when` they were produced.  __All__ must have the
 * same `entity` or the [[com.github.osxhacker.foundation.models.core.Scenario]] will
 * fail with an [[com.github.osxhacker.foundation.models.core.error.ApplicationError]].
 *
 * @author osxhacker
 *
 */
final case class SortEntityEvents[E <: Entity[E], A <: EntityEvent[E]]
	(private val events : Seq[A])
	extends Scenario[ApplicationError \/ Seq[A]]
{
	/// Class Imports
	import syntax.all._
	import syntax.std.boolean._


	/// Instance Properties
	private val finiteSize : Seq[A] => ApplicationError \/ Seq[A] =
		as => as.hasDefiniteSize either as or LogicError ("infinite stream");

	private val sameEntities : Seq[A] => ApplicationError \/ Seq[A] =
		as => {
			if (as.size < 2)
				\/- (as);
			else
				as.foldLeft (as.head.entity.right[ApplicationError]) {
					(state, a) =>

					state.ensure (DomainValueError ("mixed event entities")) {
						_ === a.entity
						}
					}
					.map (_ => as);
		}


	override def apply () : ApplicationError \/ Seq[A] =
		check ().map (_.sortBy (_.when));


	private def check () = finiteSize (events) >>= sameEntities;
}

