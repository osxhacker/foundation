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

package com.github.osxhacker.foundation.models.security.vault

import scala.language.{
	higherKinds,
	postfixOps
	}

import scala.reflect.ClassTag

import scalaz.{
	:+: => _,
	Coproduct => _,
	Failure => _,
	Id => _,
	Success => _,
	_
	}

import shapeless.{
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	LogicError
	}

import com.github.osxhacker.foundation.models.security.encryption.{
	Algorithm,
	CipherKeys,
	SinglePartDecryption,
	SinglePartEncryption
	}

import com.github.osxhacker.foundation.models.security.error.{
	InvalidDecryptionKeyError,
	SecurityError,
	VaultError
	}

import com.github.osxhacker.foundation.models.core.net.Scheme


/**
 * The '''Vault''' type defines the abstraction of a physical security vault,
 * such as a safe, as it applies to securing arbitrary
 * [[com.github.osxhacker.foundation.models.security.vault.Vaulted]] types.  When an
 * object is `store`d, the "latest"
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] are used.
 * However, `retrieve` has available all known '''keys''' given during
 * construction.
 *
 * @author osxhacker
 *
 */
final class Vault[AlgoT <: Algorithm[AlgoT]] (
	private val keys : Seq[CipherKeys]
	)
	(implicit private val a : AlgoT)
{
	/// Self Type Constraints
	self =>


	/// Class Imports
	import Vault._
	import \/.fromTryCatchThrowable
	import syntax.all._
	import syntax.std.boolean._
	import syntax.std.option._


	/// Class Types
	object UnvaultObject
		extends Poly1
	{
		implicit def caseVaulted[A] : Case.Aux[Vaulted[A], Vaulted[A]] =
			at ( _.unvault (self));

		implicit def caseVaultAware[A <: VaultAware[A]]
			: Case.Aux[A, A] =
			at (_.unvault (self) valueOr (e => throw e));
	}


	object VaultObject
		extends Poly1
	{
		implicit def caseVaulted[T] : Case.Aux[Vaulted[T], Vaulted[T]] =
			at (_.vault (self));

		implicit def caseVaultAware[A <: VaultAware[A]]
			: Case.Aux[A, A] =
			at (_.vault (self) valueOr (e => throw e));
	}


	/// Instance Properties
	private val decryptors =
		keys map (k => (k.version, new SinglePartDecryption[AlgoT] (k))) toMap;

	private val encryptionKey = keys.maxBy (_.version);
	private val encryptor = new SinglePartEncryption[AlgoT] (encryptionKey);


	def retrieve[T] (value : Encrypted[T]) : ApplicationError \/ Decrypted[T] =
		decryptors.get (value.version) cata (
			decryptor => decryptor (value.content) map (new Decrypted[T] (_)),
			InvalidDecryptionKeyError (
				"unknown key version encrypted=%s".format (
					value.version
					)
				).left
			);


	def retrieve[T <: AnyRef] (instance : T)
		(implicit e : Everywhere[UnvaultObject.type, T], ct : ClassTag[T])
		: ApplicationError \/ T =
		fromTryCatchThrowable[e.Result, Throwable] {
			e (instance);
			} |> (mapResult[e.Result, T] _);


	def store[T] (value : Decrypted[T]) : ApplicationError \/ Encrypted[T] =
		encryptor (value.content) map {
			new Encrypted[T] (encryptionKey.version, _);
			}


	def store[T <: AnyRef] (instance : T)
		(implicit e : Everywhere[VaultObject.type, T], ct : ClassTag[T])
		: ApplicationError \/ T =
		fromTryCatchThrowable[e.Result, Throwable] {
			e (instance);
			} |> (mapResult[e.Result, T] _);


	private def mapResult[A, B] (result : Throwable \/ A)
		(implicit CT : ClassTag[B])
		: ApplicationError \/ B =
		result match {
			case \/- (b : B) =>
				\/- (b);

			case \/- (other : AnyRef) =>
				-\/ (
					LogicError (
						s"unexpected vaulting result: ${other.getClass}"
						)
					);
				
			case -\/ (err) =>
				-\/ (VaultError (err.getMessage, Option (err)));
			}
}


object Vault
{
	/// Instance Properties
	def scheme[A <: Algorithm[A]] (implicit a : A) : Scheme[Vault[A]] =
		Scheme (s"vault-${a.name.toLowerCase}");


	/**
	 * The apply method is provided to support functional-style creation of a
	 * [[com.github.osxhacker.foundation.models.security.vault.Vault]].
	 */
	def apply[A <: Algorithm[A]] (keys : Seq[CipherKeys])
		(implicit a : A)
		: Vault[A] =
		new Vault[A] (keys);
}

