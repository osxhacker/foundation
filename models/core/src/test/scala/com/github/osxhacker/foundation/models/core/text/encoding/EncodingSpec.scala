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

package com.github.osxhacker.foundation.models.core.text.encoding

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec


/**
 * The '''EncodingSpec''' trait defines the common unit tests ''all''
 * [[com.github.osxhacker.foundation.models.core.text.encoding.Encoder]]s and
 * [[com.github.osxhacker.foundation.models.core.text.encoding.Decoder]]s must support.
 *
 * @author osxhacker
 *
 */
trait EncodingSpec
	extends ProjectSpec
{
	def byteProcessor[A <: Encoder[Array[Byte]] with Decoder[Array[Byte]]] (
		processor : A
		)
	{
		"given an empty byte array" should {
			"produce an empty string" in {
				val result = processor.encode (new Array[Byte] (0));
				
				assert (result ne null);
				assert (result.isEmpty);
				}
			}
	}

	
	def stringProcessor[A <: Encoder[String] with Decoder[String]] (
		processor : A,
		encodedLength : Int
		)
	{
		"given an empty string" should {
			"produce an empty string" in {
				val result = processor.encode ("");
				
				assert (result ne null);
				assert (result.isEmpty);
				}
			}

		"encoding a 10 character string" must {
			val input = "1234567890";
			val encoded = processor.encode (input);

			s"produce a ${encodedLength} character length string" in {
				assert (
					encoded.length === encodedLength,
					s"expected ${encodedLength} characters in [${encoded}]"
					);
				}

			"be able to decode it" in {
				val decoded = processor.decode (encoded);
				
				assert (decoded === input);
				}
			}
	}
}
