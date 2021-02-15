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

package com.github.osxhacker.foundation.models.core.time

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.nscala_time.time.{
	Implicits => NScalaTimeImplicits
	}


/**
 * The '''Implicits''' type insulates the system from specific knowledge of how
 * [[http://joda-time.sourceforge.net/userguide.html Joda-Time]] types are
 * decorated in order to support native Scala syntax.
 *
 * @author osxhacker
 */
trait Implicits
	extends NScalaTimeImplicits
{
	/// Class Imports
	import scalaz.std.anyVal._
	import scalaz.syntax.equal._
	import types._


	/// Class Types


	/// Instance Properties
	
	
	/// Implicit Conversions
	implicit def abstractInstantEqual[A <: AbstractInstant] : Equal[A] =
		Equal.equal[A] (_.getMillis === _.getMillis);

	implicit def readableInstantEqual[A <: ReadableInstant] : Equal[A] =
		Equal.equal[A] (_.getMillis === _.getMillis);
}
