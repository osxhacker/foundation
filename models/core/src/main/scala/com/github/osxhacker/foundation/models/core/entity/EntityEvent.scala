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

package com.github.osxhacker.foundation.models.core.entity

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ChronologicalEvent
import com.github.osxhacker.foundation.models.core.time


/**
 * The '''EntityEvent''' trait defines the Domain Object Model abstraction of
 * a [[com.github.osxhacker.foundation.models.core.ChronologicalEvent]] which relates to
 * a specific [[com.github.osxhacker.foundation.models.core.entity.Entity]].
 *
 * @author osxhacker
 *
 */
trait EntityEvent[A <: Entity[A]]
	extends ChronologicalEvent
{
	/// Instance Properties
	val entity : EntityRef[A];
}


object EntityEvent
{
	/// Class Imports
	import syntax.equal._
	import time.implicits._


	/// Implicit Conversions
	implicit def EntityEventEqual[A <: Entity[A], B <: EntityEvent[A]]
		: Equal[B] =
		Equal.equal {
			(a, b) =>

			a.entity === b.entity && a.when === b.when;
			}
}
