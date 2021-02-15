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

import scalaz.{
	Failure => _,
	Ordering => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import time.types.ReadableInstant


/**
 * The '''ChronologicalEvent''' trait defines a refinement of
 * [[com.github.osxhacker.foundation.models.core.DomainEvent]] which mandates knowledge of
 * `when` it was __originally__ produced.  Each type of
 * '''ChronologicalEvent''' defines its ordering in terms of `when` it was
 * produced.
 *
 * @author osxhacker
 *
 */
trait ChronologicalEvent
	extends DomainEvent
{
	/// Instance Properties
	val when : ReadableInstant;


	/// Java Accessors
	def getWhen () : ReadableInstant = when;
}


object ChronologicalEvent
{
	/// Class Imports
	import time.implicits._


	/// Implicit Conversions
	implicit def ChronologicalEventEqual[A <: ChronologicalEvent]
		: Equal[A] =
		Equal.equalBy (_.when);

	implicit def ChronologicalEventOrdering[A <: ChronologicalEvent]
		: Ordering[A] =
		Ordering.by (_.when);

	implicit val ChronologicalEventShow : Show[ChronologicalEvent] =
		Show.showFromToString;
}
