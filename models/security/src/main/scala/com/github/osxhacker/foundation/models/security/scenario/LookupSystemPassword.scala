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

package com.github.osxhacker.foundation.models.security.scenario

import scala.concurrent.duration.FiniteDuration

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.akkax.{
	ActorSystemAware,
	streams
	}

import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.net.URI
import com.github.osxhacker.foundation.models.security.password.SystemPassword
import com.github.osxhacker.foundation.models.security.akkax.{
	FindPasswordRequest,
	FindPasswordResponse
	}

import com.github.osxhacker.foundation.models.security.akkax.extension.SystemPasswordEntries


/**
 * The '''LookupSystemPassword''' type defines the
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for resolving a
 * [[com.github.osxhacker.foundation.models.security.SystemPassword]] based on a
 * logical [[com.github.osxhacker.foundation.models.core.net.URI]].
 *
 * @author osxhacker
 *
 */
final case class LookupSystemPassword (private val timeout : FiniteDuration)
	(implicit override val system : ActorSystem)
	extends Scenario[Flow[URI, SystemPassword, NotUsed]]
		with ActorSystemAware
{
	/// Class Imports
	import streams._
	import functional.futures._
	import functional.futures.monad._


	/// Class Types
	type ResultType = Flow[URI, SystemPassword, NotUsed]


	/// Instance Properties
	implicit private val t = timeout;
	private val steps : ResultType =
		Flow[URI].requestOfClient (SystemPasswordEntries (system)) {
			uri =>

			FindPasswordRequest (uri);
			}
			.map (_.result)
			.valueOrFail ();


	override def apply () : ResultType = steps;
}
