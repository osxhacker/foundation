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

package com.github.osxhacker.foundation.models.core.scenario

import java.security.MessageDigest
import java.util.UUID.randomUUID
import java.util.concurrent.atomic.AtomicInteger

import scala.compat.Platform

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	Identifier,
	Scenario
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.net.Scheme
import com.github.osxhacker.foundation.models.core.text.encoding.Hex


/**
 * The '''GenerateUniqueIdentifier''' type defines a
 * [[com.github.osxhacker.foundation.models.core.Scenario]] which is responsible for
 * producing a unique [[com.github.osxhacker.foundation.models.core.Identifier]]
 * within an acceptable probability of `1/2^128` for collisions.
 *
 * Note that while [[java.security.MessageDigest]] is '''not''' thread-safe,
 * '''GenerateUniqueIdentifier''' is due to invoking `clone` on each `apply`.
 *
 * @author osxhacker
 */
final case class GenerateUniqueIdentifier[A] (
	private val scheme : Scheme[A]
	)
	extends Scenario[ApplicationError \/ Identifier]
{
	/// Class Imports
	import GenerateUniqueIdentifier._
	import scalaz.syntax.id._
	
	
	/// Instance Properties
	private val hex = Hex[Array[Byte]] ();


	override def apply () : ApplicationError \/ Identifier =
		Identifier (scheme, generate ());
	
	
	private def generate () : String =
	{
		val md = digest.clone ().asInstanceOf[MessageDigest];
		val buffer = new StringBuilder ();
		
		val input = buffer.append (randomUUID)
			.append ('-')
			.append (Platform.currentTime)
			.append ('-')
			.append (counter.incrementAndGet ())
			.toString ()
			.getBytes ("UTF-8");
		
		return md.digest (input) |> hex.encode;
	}
}


object GenerateUniqueIdentifier
{
	/// Instance Properties
	private val digest = MessageDigest.getInstance ("MD5");
	private val counter = new AtomicInteger ();
}
