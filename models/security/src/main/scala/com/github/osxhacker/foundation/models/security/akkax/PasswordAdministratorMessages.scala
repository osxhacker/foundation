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

package com.github.osxhacker.foundation.models.security.akkax

import scala.language.higherKinds

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.{
	Message,
	Request,
	Response
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.net.URI
import com.github.osxhacker.foundation.models.security.password.{
	CleartextPassword,
	StorageIndicator,
	SystemPassword
	}


/**
 * The '''PasswordAdministratorMessage''' trait defines the common ancestor for
 * __all__ one-way [[com.github.osxhacker.foundation.models.core.akkax.Message]]s
 * understood by the
 * [[com.github.osxhacker.foundation.models.security.akkax.PasswordAdministrator]]
 * [[akka.actor.Actor]].
 *
 * @author osxhacker
 *
 */
sealed trait PasswordAdministratorMessage
	extends Message[PasswordAdministratorMessage]


/**
 * The '''PasswordAdministratorRequest''' trait defines the common ancestor for
 * __all__ [[com.github.osxhacker.foundation.models.core.akkax.Request]]s understood
 * by the [[com.github.osxhacker.foundation.models.security.akkax.PasswordAdministrator]]
 * [[akka.actor.Actor]].
 *
 * @author osxhacker
 *
 */
sealed trait PasswordAdministratorRequest[
	R <: PasswordAdministratorResponse[R]
	]
	extends Request[R]


/**
 * The '''PasswordAdministratorResponse''' trait defines the common ancestor for
 * __all__ [[com.github.osxhacker.foundation.models.core.akkax.Response]]s understood
 * by the [[com.github.osxhacker.foundation.models.security.akkax.PasswordAdministrator]]
 * [[akka.actor.Actor]].
 *
 * @author osxhacker
 *
 */
sealed trait PasswordAdministratorResponse[
	R <: PasswordAdministratorResponse[R]
	]
	extends Response[R]


final case class DiscoveredPasswords (
	val known : Map[URI @@ StorageIndicator, CleartextPassword]
	)
	extends PasswordAdministratorMessage


final case class FindPasswordRequest (val logical : URI)
	extends PasswordAdministratorRequest[FindPasswordResponse]


final case class FindPasswordResponse (
	val result : ApplicationError \/ SystemPassword
	)
	extends PasswordAdministratorResponse[FindPasswordResponse]


final case class ListPasswordsRequest ()
	extends PasswordAdministratorRequest[ListPasswordsResponse]


final case class ListPasswordsResponse (
	val result : ApplicationError \/ Seq[URI]
	)
	extends PasswordAdministratorResponse[ListPasswordsResponse]


