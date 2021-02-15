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

import scala.language.postfixOps

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec
import com.github.osxhacker.foundation.models.core.net.Scheme


/**
 * The '''GenerateUniqueIdentifierSpec''' type defines the unit test suite for
 * certifying the
 * [[com.github.osxhacker.foundation.models.core.scenario.GenerateUniqueIdentifier]]
 * for fitness of purpose as well as serving as an exemplar for its use.
 *
 * @author osxhacker
 */
final class GenerateUniqueIdentifierSpec
	extends ProjectSpec
{
	/// Instance Properties
	val scheme = Scheme[GenerateUniqueIdentifierSpec] ("test");
	
	
	"A GenerateUniqueIdentifier" must {
		"be able to make a valid Identifier" in {
			val generator = new GenerateUniqueIdentifier (scheme);

			assert (generator ().isRight);
			}

		"produce a constant-size Identifier" in {
			val generator = new GenerateUniqueIdentifier (scheme);
			val ids = (0 until 100) map (_ => generator () orThrow);
			val lengths = ids map (_.withoutScheme ().length) toSet;
			
			assert (lengths.size === 1);
			assert (lengths.head > 0);
			}
		}
}
