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

import org.apache.commons.codec.binary.{
	Base32 => CommonsBase32
	}


/**
 * The '''Base32''' trait defines an
 * [[com.github.osxhacker.foundation.models.core.encoding.Encoder]] and
 * [[com.github.osxhacker.foundation.models.core.encoding.Decoder]] in terms of
 * [[https://tools.ietf.org/html/rfc4648#section-6 Base 32 Encoding]].
 *
 * @author osxhacker
 *
 */
trait Base32[A]
	extends Encoder[A]
		with Decoder[A]


object Base32
	extends Base32Implicits
{
	/**
	 * The apply method is provided so that a '''Base32''' instance can be
	 * summoned as needed and have the proper signature.
	 */
	def apply[A] () (implicit a : Base32[A]) : Base32[A] = a;
}


trait Base32Implicits
{
	protected def instance[A] (
		encoder : A => String,
		decoder : String => A
		)
		: Base32[A] =
		new Base32[A] {
			override def decode (s : String) : A = decoder (s);
			override def encode (a : A) : String = encoder (a);
			}


	/// Implicit Conversions
	implicit val bytesBase32 = instance[Array[Byte]] (
		bs => new CommonsBase32 (0).encodeAsString (bs),
		enc => new CommonsBase32 (0).decode (enc)
		);


	implicit val stringBase32 = instance[String] (
		s => new CommonsBase32 (0).encodeAsString (s.getBytes),
		enc => new String (new CommonsBase32 (0).decode (enc))
		);
}
