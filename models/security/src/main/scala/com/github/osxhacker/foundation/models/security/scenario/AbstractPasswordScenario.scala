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

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.text.encoding.Base64
import com.github.osxhacker.foundation.models.security.password.{
	Password,
	Salt
	}

import com.github.osxhacker.foundation.models.security.hash.SHA256


/**
 * The '''AbstractPasswordScenario''' type defines the common
 * [[com.github.osxhacker.foundation.models.core.Scenario]] for
 * [[com.github.osxhacker.foundation.models.security.Password]]-related use-cases.
 *
 * @author osxhacker
 *
 */
abstract class AbstractPasswordScenario[+A] ()
	extends Scenario[A]
{
	/// Class Types
	type EncodingType[X] = Base64[X]
	type HashType = SHA256
	type SaltLength = Salt.DefaultLength


	/// Instance Properties
	
}
