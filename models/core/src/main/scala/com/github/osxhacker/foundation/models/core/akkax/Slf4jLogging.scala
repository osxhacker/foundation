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

import scala.util.Failure
import scala.util.control.Exception._

import akka.actor.Actor
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import org.slf4j.{
	Logger,
	LoggerFactory,
	MDC
	}


/**
 * The '''Slf4jLogging''' type provides SLF4j-based logging to an
 * [[com.github.osxhacker.foundation.models.core.akkax.ActorStack]].  Only this
 * type is aware of SLF4j, thus insulating the rest of the system.
 *
 * @author osxhacker
 *
 */
trait Slf4jLogging
{
	/// Self Type Constraints
	this : Actor =>


	/// Class Imports
	import syntax.std.boolean._


	/// Instance Properties
	private val logger = LoggerFactory.getLogger (getClass);
	private val label = "actorPath";
	private val path = self.path.toString;


	/**
	 * The debug method is provided to insulate types from having a direct
	 * dependency on the logging API.
	 */
	protected def debug (message : String, args : Any *) : Unit =
		logger.isDebugEnabled.when {
			withPathContext (logger.debug (formatted (message, args)));
			}


	/**
	 * The error method is provided to insulate types from having a direct
	 * dependency on the logging API.
	 */
	protected def error (message : String, args : Any *) : Unit =
		logger.isErrorEnabled.when {
			withPathContext (logger.error (formatted (message, args)));
			}


	/**
	 * The error method is provided to insulate types from having a direct
	 * dependency on the logging API.  This version allows the caller to
	 * provide a `Failure` instance.
	 */
	protected def error[A] (f : Failure[A], message : String, args : Any *)
		: Unit =
		logger.isErrorEnabled.when {
			withPathContext (
				logger.error (formatted (message, args), f.exception)
				);
			}


	/**
	 * The error method is provided to insulate types from having a direct
	 * dependency on the logging API.  This version allows the caller to
	 * provide a `Throwable` instance.
	 */
	protected def error (ex : Throwable, message : String, args : Any *)
		: Unit =
		logger.isErrorEnabled.when {
			withPathContext (logger.error (formatted (message, args), ex));
			}


	/**
	 * The info method is provided to insulate types from having a direct
	 * dependency on the logging API.
	 */
	protected def info (message : String, args : Any *) : Unit =
		logger.isInfoEnabled.when {
			withPathContext (logger.info (formatted (message, args)));
			}


	/**
	 * The trace method is provided to insulate types from having a direct
	 * dependency on the logging API.
	 */
	protected def trace (message : String, args : Any *) : Unit =
		logger.isTraceEnabled.when {
			withPathContext (logger.trace (formatted (message, args)));
			}


	/**
	 * The warn method is provided to insulate types from having a direct
	 * dependency on the logging API.
	 */
	protected def warn (message : String, args : Any *) : Unit =
		logger.isWarnEnabled.when {
			withPathContext (logger.warn (formatted (message, args)));
			}


	private def formatted (template : String, args : Seq[Any]) : String =
		template.replaceAllLiterally ("{}", "%s").format (args : _*);


	private def withPathContext (block : => Unit) : Unit =
		ultimately (MDC.remove (label)) {
			MDC.put (label, path);
			block;
			}
}

