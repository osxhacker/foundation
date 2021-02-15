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

import org.scalatest.WordSpecLike
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.{
	ActorBasedSpec,
	MaterializerAware
	}

import com.github.osxhacker.foundation.models.core.akkax.extension.SystemWideMaterializer
import com.github.osxhacker.foundation.models.security.SecuritySettings
import com.github.osxhacker.foundation.models.security.encryption._
import com.github.osxhacker.foundation.models.security.vault.VaultSupport


/**
 * The '''VaultAdministratorOperationsSpec''' type defines the unit tests
 * which certify the
 * [[com.github.osxhacker.foundation.models.security.akkax.VaultAdministratorOperations]]
 * behaviour for fitness of purpose and to serve as an exemplar of use.
 *
 * @author osxhacker
 *
 */
final class VaultAdministratorOperationsSpec
	extends ActorBasedSpec ("test-vault-administrator-operations")
		with MaterializerAware
		with VaultAdministratorOperations[AES]
		with VaultSupport
		with WordSpecLike
{
	/// Class Imports
	import syntax.all._


	/// Instance Properties
	implicit lazy val materializer = SystemWideMaterializer (system).materializer;
	implicit lazy val keyStorage = MockKeyStorage[AES] ();

	lazy val settings = SecuritySettings (system);


	"The loadKnownKeys operation" must {
		"be able to resolve keys from a storage provider" in {
			loadKnownKeys (testActor, settings);
			
			val response = expectMsgType[LoadingKeysResult] (
				settings.loadingTimeout
				);

			assert (response.result.isRight, response.toString);
			response.result foreach {
				keys =>

				assert (!keys.isEmpty);
				}
			}
		}
	
	"The createVaultsFromKeys operation" must {
		"create an entry for each known CipherKeys instance" in {
			val mapped = createVaultFromKeys (keyStorage.keys);

			assert (mapped ne null);
			}
		}
}
