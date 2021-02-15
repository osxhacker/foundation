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

import scala.collection.mutable.MutableList
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.NotUsed
import akka.actor.ActorRefFactory
import akka.stream.{
	ActorMaterializer,
	Materializer
	}

import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import org.scalatest._

import com.github.osxhacker.foundation.models.core.{
	DeploymentEnvironment,
	Identifier
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional._
import com.github.osxhacker.foundation.models.security.encryption._
import com.github.osxhacker.foundation.models.security.scenario.CreateVault


/**
 * The '''VaultSupport''' trait defines supporting types useful in defining
 * both behavior and unit tests which collaborate with
 * `com.github.osxhacker.foundation.models.security.vault` types.
 *
 * @author osxhacker
 *
 */
trait VaultSupport
	extends SuiteMixin
{
	/// Self Type Constraints
	this : Suite =>


	/// Class Imports


	/// Class Types
	case class MockKeyStorage[A <: Algorithm[A]] ()
		(implicit generator : RandomKeyGenerator[A])
		extends KeyStorage
	{
		/// Class Imports
		import scalaz.syntax.either._


		/// Instance Properties
		val keys : MutableList[CipherKeys] =
			MutableList (generator (KeyVersion (42)));


		override def load ()
			(implicit m : Materializer)
			: Source[CipherKeys, NotUsed] =
			load (DeploymentEnvironment.UnitTest);


		override def load (deployment : DeploymentEnvironment)
			(implicit m : Materializer)
			: Source[CipherKeys, NotUsed] =
			Source.fromIterator (() => keys.toList.iterator);


		override def generate ()
			(implicit m : Materializer)
			: Source[CipherKeys, NotUsed] =
			generate (DeploymentEnvironment.UnitTest);


		override def generate (deployment : DeploymentEnvironment)
			(implicit m : Materializer)
			: Source[CipherKeys, NotUsed] =
		{
			keys += generator (keys.maxBy (_.version).version.next ());

			return load (deployment);
		}
	}


	case class SensitiveInfo (val what : Vaulted[String])


	case class SamplePerson (
		val name : SensitiveInfo,
		val age : Option[Vaulted[String]]
		)


	case class VaultFactory (name : String)
		(implicit factory : ActorRefFactory, storage : KeyStorage)
	{
		/// Class Imports
		import futures.comonad._
		import syntax.all._


		/// Instance Properties
		implicit val ec = ExecutionContext.global;
		implicit val expiry = 5 seconds fromNow;
		implicit val materializer = ActorMaterializer ();

		val vaultId = Identifier (Vault.scheme[AES], "test");
		lazy val safe = CreateVault[AES] (DeploymentEnvironment.UnitTest).run
			.run
			.copoint;


		def apply () : ApplicationError \/ Vault[AES] = safe;
	}
}

