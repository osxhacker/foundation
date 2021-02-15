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
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.{
	Message,
	Request,
	Response
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.security.encryption.{
	AES,
	CipherKeys,
	KeyVersion
	}

import com.github.osxhacker.foundation.models.security.vault.Vault


/**
 * The '''VaultAdministratorRequest''' trait defines the common ancestor for
 * __all__ [[com.github.osxhacker.foundation.models.core.akkax.Request]]s understood
 * by the [[com.github.osxhacker.foundation.models.security.akkax.VaultAdministrator]]
 * [[akka.actor.Actor]].
 *
 * @author osxhacker
 *
 */
sealed trait VaultAdministratorRequest[R <: VaultAdministratorResponse[R]]
	extends Request[R]
{
	/// Class Imports


	/// Class Types


	/// Instance Properties
	
}


/**
 * The '''VaultAdministratorResponse''' trait defines the common ancestor for
 * __all__ [[com.github.osxhacker.foundation.models.core.akkax.Response]]s produced
 * by the [[com.github.osxhacker.foundation.models.security.akkax.VaultAdministrator]]
 * [[akka.actor.Actor]].
 *
 * @author osxhacker
 *
 */
sealed trait VaultAdministratorResponse[R <: VaultAdministratorResponse[R]]
	extends Response[R]
{
	/// Class Imports


	/// Class Types


	/// Instance Properties
	
}


final case class LoadingKeysResult (
	val result : ApplicationError \/ Seq[CipherKeys]
	)
	extends Message[LoadingKeysResult]


object LoadingKeysResult
{
	def apply (error : ApplicationError) : LoadingKeysResult =
		new LoadingKeysResult (-\/ (error));
}


final case class MostRecentVaultRequest ()
	extends VaultAdministratorRequest[MostRecentVaultResponse]


final case class MostRecentVaultResponse (
	val result : ApplicationError \/ Vault[AES]
	)
	extends VaultAdministratorResponse[MostRecentVaultResponse]
	