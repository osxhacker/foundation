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

import javax.crypto.spec.SecretKeySpec
import java.security.Key

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import shapeless.{
	syntax => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError
	}

import com.github.osxhacker.foundation.models.core.text.encoding.Base64
import com.github.osxhacker.foundation.models.security.encryption.{
	Algorithm,
	CipherKeys,
	KeyVersion
	}


/**
 * The '''InternalizeCipherKeys''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for producing a
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] instance
 * from its external representation.
 * 
 * @author osxhacker
 *
 * @see com.github.osxhacker.foundation.models.security.scenario.ExternalizeCipherKeys
 */
final case class InternalizeCipherKeys[AlgoT <: Algorithm[AlgoT]] (
	private val version : KeyVersion,
	private val properties : Map[String, String]
	)
	(implicit private val algo : AlgoT)
	extends Scenario[ApplicationError \/ CipherKeys]
{
	/// Class Imports
	import std.option._
	import syntax.all._
	import syntax.std.option._


	/// Instance Properties
	private val decoder = Base64[Array[Byte]] ();


	def apply () : ApplicationError \/ CipherKeys =
	{
		(
			(properties.get ("secret").map (createKey) \/> missing ("secret")) |@|
			properties.get ("shared").map (createKey).right |@|
			properties.get ("iv").map (decoder.decode).right
		) (CipherKeys (version, _, _, _));
	}


	private def createKey (encoded : String) : Key =
	{
		val decoded = decoder.decode(encoded)

		return new SecretKeySpec (decoded, 0, decoded.length, algo.name);
	}


	private def missing (property : String) : ApplicationError =
		DomainValueError (s"missing property: ${property}");
}


object InternalizeCipherKeys
{
	/**
	 * This version of the apply method exists to provide syntactic sugar for
	 * situations where the required collaborators are in an `HList`.
	 */
	def apply[AlgoT <: Algorithm[AlgoT]] (
		hlist : KeyVersion :: Map[String, String] :: HNil
		)
		(implicit algo : AlgoT)
		: InternalizeCipherKeys[AlgoT] =
		new InternalizeCipherKeys (hlist.head, hlist.tail.head);


	/**
	 * This version of the apply method exists to provide syntactic sugar for
	 * situations where the required collaborators are in a `Tuple`.
	 */
	def apply[AlgoT <: Algorithm[AlgoT]] (tuple : (KeyVersion, Map[String, String]))
		(implicit algo : AlgoT)
		: InternalizeCipherKeys[AlgoT] =
		new InternalizeCipherKeys (tuple._1, tuple._2);
}

