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

import org.joda.time._
import org.joda.time.format.DateTimeFormatter


/**
 * The '''StaticInstant''' type defines methods useful in using/manipulating
 * [[http://www.joda.org/joda-time/apidocs/org/joda/time/Instant.html Instant]]s.
 *
 * @author osxhacker
 */
trait StaticInstant
{
	/// Class Imports
	import implicits._


	/// Class Types


	def epoch () : Instant = new Instant (0L);
	def now () : Instant = new Instant ();
	def parse (str: String) : Instant = Instant.parse (str);
	def parse (str: String, formatter: DateTimeFormatter) : Instant =
		Instant.parse (str, formatter);

	def nextSecond () = now () + 1.second;
	def nextMinute () = now () + 1.minute;
	def nextHour () = now () + 1.hour;

	def lastSecond () = now () - 1.second;
	def lastMinute () = now () - 1.minute;
	def lastHour () = now () - 1.hour;
}


object StaticInstant
	extends StaticInstant
	
	