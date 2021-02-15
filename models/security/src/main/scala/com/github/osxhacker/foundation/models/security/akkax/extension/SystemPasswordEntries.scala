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

package com.github.osxhacker.foundation.models.security.akkax.extension

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.MessagingExtension
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.akkax.{
	PasswordAdministrator,
	PasswordAdministratorRequest,
	PasswordAdministratorResponse
	}


/**
 * The '''SystemPasswordEntries''' type defines an [[akka.actor.Extension]]
 * which provides a
 * [[com.github.osxhacker.foundation.models.security.akkax.PasswordAdministrator]] in the
 * [[akka.actor.ActorSystem]].
 *
 * @author osxhacker
 *
 */
final class SystemPasswordEntries (override protected val actor : ActorRef)
	(implicit override protected val factory : ActorRefFactory)
	extends MessagingExtension[
		PasswordAdministratorResponse,
		PasswordAdministratorRequest
		] (actor)


object SystemPasswordEntries
	extends ExtensionId[SystemPasswordEntries]
		with ExtensionIdProvider
{
	/// Class Imports
	import functional._
	import functional.futures.comonad._
	import syntax.comonad._


	override def createExtension (system : ExtendedActorSystem)
		: SystemPasswordEntries =
		new SystemPasswordEntries (PasswordAdministrator () (system)) (
			system
			);


	override def lookup () = this;
}

