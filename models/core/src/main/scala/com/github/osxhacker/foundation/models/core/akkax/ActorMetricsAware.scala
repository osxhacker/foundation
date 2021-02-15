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

package com.github.osxhacker.foundation.models.core.akkax

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.metrics.MetricsAware


/**
 * The '''ActorMetricsAware''' type defines a
 * [[com.github.osxhacker.foundation.models.core.akkax.Stackable]] `trait` required
 * by other components which deliver specific `metrics` regarding an
 * [[akka.actor.Actor]], such as lifecycle, messages received, etc.  As such,
 * behaviour specific to defining [[akka.actor.Actor]] `metrics` is provided
 * here.
 *
 * @author osxhacker
 */
trait ActorMetricsAware
	extends Stackable
		with MetricsAware
{
	/// Self Type Constraints
	this : ActorStack =>


	/// Class Imports


	/// Class Types


	/// Instance Properties
	
	
	/**
	 * According to the metrics-scala library, gauges cannot be created
	 * with duplicate names, so ensure they are unregistered.
	 */
	abstract override def preRestart (
		reason : Throwable,
		message : Option[Any]
		)
		: Unit =
	{
		metrics.unregisterGauges ();
		
		super.preRestart (reason, message);
	}
}

