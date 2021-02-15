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

package com.github.osxhacker.foundation.models.security.encryption

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import org.scalatest._


/**
 * The '''AlgorithmSupport''' trait defines
 * [[http://doc.scalatest.org/3.0.1/#org.scalatest.FeatureSpec fixture context]]
 * types which help reduce boilerplate test logic.
 *
 * @author osxhacker
 *
 */
trait AlgorithmSupport
	extends SuiteMixin
{
	/// Self Type Constraints
	this : Suite =>


	/// Class Imports


	/// Class Types
	sealed class AlgorithmFixture[A <: Algorithm[A]] ()
		(
			implicit val algorithm : A,
			val generator : RandomKeyGenerator[A]
		)
	{
	}
	
	
	def algorithmFor[A <: Algorithm[A]] ()
		(implicit algorithm : A, generator : RandomKeyGenerator[A])
		: AlgorithmFixture[A] =
		new AlgorithmFixture[A] ();
}
