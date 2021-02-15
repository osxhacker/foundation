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

import java.security.Key

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.text.encoding.Base64
import com.github.osxhacker.foundation.models.security.encryption.{
	Algorithm,
	CipherKeys,
	KeyVersion
	}


/**
 * The '''ExternalizeCipherKeys''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for producing an
 * external representation of a
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] instance.
 *
 * @author osxhacker
 *
 * @see com.github.osxhacker.foundation.models.security.scenario.InternalizeCipherKeys
 */
final case class ExternalizeCipherKeys[AlgoT <: Algorithm[AlgoT]] (
	private val keys : CipherKeys
	)
	(implicit private val algo : AlgoT)
	extends Scenario[(KeyVersion, Map[String, String])]
{
	/// Class Imports
	import syntax.all._


	/// Instance Properties
	private val encoder = Base64[Array[Byte]] ();


	override def apply () : (KeyVersion, Map[String, String]) =
		keys.version -> (
			Map (encodeKey ("secret") (keys.secret)) ++
				keys.shared.map (encodeKey ("shared")).toMap ++
				keys.initializationVector.map (encodeBytes ("iv")).toMap
			);


	private def encodeBytes (name : String)
		(bytes : Array[Byte]) : (String, String) =
		name -> encoder.encode (bytes);


	private def encodeKey (name : String)
		(key : Key) : (String, String) =
		encodeBytes (name) (key.getEncoded);
}
