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

package com.github.osxhacker.foundation.models.core.text.format

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''JavaPropertiesSpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.core.text.format.JavaProperties]] type for
 * fitness of purpose.
 *
 * @author osxhacker
 *
 */
final class JavaPropertiesSpec
	extends ProjectSpec
{
	/// Class Imports


	/// Class Types
	case class SomeProperties (
		val name : String,
		val someBytes : Array[Byte],
		val aFloat : Float
		)
		
		
	case class Wrapper (props : SomeProperties)


	"JavaProperties" must {
		"support byte arrays" in {
			val bytes = Array[Byte] (0x01, 0x02, 0x03, 0x04);
			val result = Formatter[JavaProperties, Array[Byte]] ().write (
				bytes
				);

			assert (result ne null);
			}

		"support simple ADT's" in {
			val instance = SomeProperties (
				name = "this is just a test",
				someBytes = Array[Byte] (0x42, 0x00, 0x79, 0x02),
				aFloat = 4.56f
				);
			
			val result = JavaProperties (instance);
			
			assert (result.contains ("name=this is just a test"), result);
			assert (result.contains ("someBytes="), result);
			assert (result.contains ("aFloat=4.56"), result);
			}

		"support wrapped ADT's" in {
			val instance = Wrapper (
				SomeProperties (
					name = "this is just a test",
					someBytes = Array[Byte] (0x42, 0x00, 0x79, 0x02),
					aFloat = 4.56f
					)
				);
			
			val result = JavaProperties (instance);

			assert (result.contains ("props.name=this is just a test"), result);
			assert (result.contains ("props.someBytes="), result);
			assert (result.contains ("props.aFloat=4.56"), result);
			}
		}
}
