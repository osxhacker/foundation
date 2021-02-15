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

package com.github.osxhacker.foundation.models.core.akkax.extension

import akka.actor._
import com.codahale.metrics.jmx.JmxReporter

import com.github.osxhacker.foundation.models.core.metrics.MetricsAware


/**
 * The '''JmxReporting''' type defines an Akka [[akka.actor.Extension]] which
 * initializes components which interact with
 * [[https://docs.oracle.com/javase/tutorial/jmx/ Java JMX]].
 *
 * @author osxhacker
 */
sealed class JmxReporting ()
	extends Extension
		with MetricsAware
{
	/// Instance Properties
	final val reporter = {
		val instance = JmxReporter.forRegistry (metrics.registry).build ();
		
		instance.start ();
		instance;
		}
}


object JmxReporting
	extends ExtensionId[JmxReporting]
		with ExtensionIdProvider
{
	override def createExtension (system : ExtendedActorSystem)
		: JmxReporting =
		new JmxReporting ();
	
	
	override def lookup () = this;
}
