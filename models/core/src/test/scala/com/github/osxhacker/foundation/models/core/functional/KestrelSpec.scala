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

package com.github.osxhacker.foundation.models.core
package functional

import scala.language.postfixOps

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''KestrelSpec''' type defines the unit tests for verifying the
 * [[com.github.osxhacker.foundation.models.core.functional.Kestrel]] type.
 *
 * @author osxhacker
 */
final class KestrelSpec
	extends ProjectSpec
{
	/// Class Imports
	import std.option._


	"A Kestrel" must {
		"support inline syntax" in {
			assertCompiles ("""
				import kestrel._
				
				Option (0).kestrel (_ + 1);
				""");
			}
		
		"support 'Either' types" in {
			assertCompiles ("""
				import kestrel._
				import syntax.either._
				
				99.right[String].kestrel { _ => 0 }
				""");
			}
		
		"execute the given functor" in {
			var n = 0;
			
			Kestrel[Option].kestrel (Some ("value")) (_ => n = 1);
			
			assert (n === 1);
			}
		
		"return the object given to it" in {
			var n = 0;
			
			val result = Kestrel[Option].kestrel (Some (99)) (_ => n = 1);
			
			assert (result === Some (99));
			}
		}
}
