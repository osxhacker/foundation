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

package com.github.osxhacker.foundation.models.core.metrics

import scala.concurrent.ExecutionContext
import scala.util.{
	Failure,
	Success
	}

import nl.grons.metrics4.scala._

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional._
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither


/**
 * The '''ServiceMethodMetrics''' type defines the Domain Object Model
 * representation of the measurements each API method, internal or
 * exposed via OSGi, will capture during system operations.
 *
 * @author osxhacker
 */
final class ServiceMethodMetrics (
	val name : String,
	val calls : Counter,
	val errors : Counter,
	val execution : Timer
	)
	extends Equals
{
	/// Class Imports
	import implicits._


	override def canEqual (that: Any) : Boolean =
		(that.asInstanceOf[AnyRef] ne null) &&
		that.isInstanceOf[ServiceMethodMetrics];
	
	
	override def equals (that : Any) : Boolean =
		canEqual (that) &&
		name.equals (that.asInstanceOf[ServiceMethodMetrics].name);
	
	
	override def hashCode () : Int = name.hashCode;


	override def toString () : String =
		"ServiceMethodMetrics(name=%s,calls=%d,errors=%d)".format (
			name,
			calls.count,
			errors.count
			);
	
	
	def apply[A] (fea : => FutureEither[A])
		(implicit EC : ExecutionContext)
		: FutureEither[A] =
	{
		calls += 1;
		
		val timed = execution.timeFutureEither[A] (fea);
		
		return timed.mapT {
			future =>

			/// Both failure of the future (timeouts, exceptions, etc.) and
			/// a success with an error payload count as an error.
			future.onComplete {
				case Success (-\/ (_)) =>
					errors += 1;
					
				case Failure (_) =>
					errors += 1;
					
				case _ => {}
				}

			future;
			}
	}


	/**
	 * Alias for `apply`.
	 */
	def run[A] (fea : => FutureEither[A])
		(implicit EC : ExecutionContext)
		: FutureEither[A] =
		apply[A] (fea);
}

