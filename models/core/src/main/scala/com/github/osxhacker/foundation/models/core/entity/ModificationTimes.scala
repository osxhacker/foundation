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

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.time.types.{
	Instant,
	ReadableInstant
	}


/**
 * The '''ModificationTimes''' type reifies when a
 * [[com.github.osxhacker.foundation.models.core.entity.Entity]] was created as well
 * as when it was last changed.
 *
 * @author osxhacker
 */
final case class ModificationTimes (
	createdOn : ReadableInstant,
	lastChanged : ReadableInstant
	)
{
	/// Class Imports


	/// Class Types


	/**
	 * The touch method creates a new '''ModificationTimes''' such that it
	 * reflects a `lastChanged` time of when this method is invoked.  The
	 * `createdOn` property ''must'' remain unchanged.
	 */
	def touch () : ModificationTimes = copy (lastChanged = new Instant);
}


object ModificationTimes
{
	/// Class Imports
	import std.option._
	
	
	/// Class Types
	object lenses
	{
		/// Class Imports
		import PLens._
		

		/// Instance Properties
		val createdOn : ModificationTimes @?> ReadableInstant =
			plensg[ModificationTimes, ReadableInstant] (
				set = mt => Option (mt) map {
					m => instant => m copy (createdOn = instant);
					},

				get = mt => Option (mt) map (_.createdOn)
				);

		val lastChanged : ModificationTimes @?> ReadableInstant =
			plensg[ModificationTimes, ReadableInstant] (
				set = mt => Option (mt) map {
					m => instant => m copy (lastChanged = instant)
					},

				get = mt => Option (mt) map (_.lastChanged)
				);
	}
	
	
	/// Instance Properties
	/**
	 * The tryCreatingFrom functor produces a functor which will
	 * [[scalaz.Apply]] the `apply` method within the container in order
	 * to create a '''ModificationTimes''' instance.
	 */
	val tryCreatingFrom : (
		Option[ReadableInstant],
		Option[ReadableInstant]
		) => Option[ModificationTimes] =
		Apply[Option].lift2 (ModificationTimes.apply _);
	
	
	/**
	 * The now method is a model of FACTORY and creates a new
	 * '''ModificationTimes''' instance having `createdOn` and `lastChanged`
	 * values of the time when the method is invoked.
	 */
	def now () : ModificationTimes =
	{
		val rightNow = new Instant;

		return ModificationTimes (rightNow, rightNow);
	}
}

