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

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.security.encryption.Algorithm


/**
 * The '''VaultAware''' trait defines the Domain Object Model contract for
 * types which can collaborate with arbitrary
 * [[com.github.osxhacker.foundation.models.security.vault.Vault]] instances.
 *
 * @author osxhacker
 *
 */
trait VaultAware[+A <: VaultAware[A]]
{
	/**
	 * The unvault method uses the provided '''vault''' to produce an instance
	 * of ''A'' in which each
	 * [[com.github.osxhacker.foundation.models.security.vault.Vaulted]] instance in the
	 * __entire__ object graph of ''A'' is decrypted.
	 */
	def unvault[AlgoT <: Algorithm[AlgoT]] (vault : Vault[AlgoT])
		: ApplicationError \/ A;


	/**
	 * The vault method uses the provided '''vault''' to produce an instance
	 * of ''A'' in which each
	 * [[com.github.osxhacker.foundation.models.security.vault.Vaulted]] instance in the
	 * __entire__ object graph of ''A'' is encrypted.
	 */
	def vault[AlgoT <: Algorithm[AlgoT]] (vault : Vault[AlgoT])
		: ApplicationError \/ A;
}
