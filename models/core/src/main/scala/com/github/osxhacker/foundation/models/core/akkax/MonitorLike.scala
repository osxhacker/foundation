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

import scala.concurrent.duration.Duration

import akka.actor._
import com.typesafe.config.ConfigException
import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}


/**
 * The '''MonitorLike''' type defines behaviour applicable to
 * [[akka.actor.Actor]]s which serve as _monitors_ of child
 * [[akka.actor.Actor]]s.  Perhaps most significantly, '''MonitorLike'''
 * ensures a consistent `supervisorStrategy` is defined for all monitor
 * [[akka.actor.Actor]]s.
 *
 * @author osxhacker
 */
trait MonitorLike
{
	/// Self Type Constraints
	this : Actor
		with Slf4jLogging
		=>


	/// Class Imports
	import SupervisorStrategy._


	/// Instance Properties
	implicit final val actorFactory : ActorRefFactory = context;

	def retries : Int;
	def timeRange : Duration;
	
	final override def supervisorStrategy =
		OneForOneStrategy (maxNrOfRetries = retries, withinTimeRange = timeRange) {
			case _ : NullPointerException =>
				error (
					"Logic error (null pointer) detected monitor={} child={}",
					self.path,
					sender ().path
					);

				Stop;

			case cause : ConfigException =>
				error (
					"Invalid subsystem configuration detected monitor={} error={}",
					self.path,
					cause
					);

				Stop;

			case cause =>
				warn (
					"Child terminated unexpectedly monitor={} child={} error={}",
					self.path,
					sender ().path,
					cause
					);

				Restart;
			}
}
