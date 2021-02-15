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

import java.security.Key
import java.security.spec.AlgorithmParameterSpec

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''CipherKeys''' type defines a value object which is responsible for
 * managing an [[scala.Option]]al `shared` and mandatory `secret`
 * [[java.security.Key]]s.  Each instance is provided with its own, distinct,
 * [[com.github.osxhacker.foundation.models.security.encryption.KeyVersion]].
 *
 * @author osxhacker
 *
 */
final case class CipherKeys (
	val version : KeyVersion,
	val secret : Key,
	val shared : Option[Key],
	val initializationVector : Option[Array[Byte]]
	)
