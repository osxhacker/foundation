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

package com.github.osxhacker.foundation.models.core.entity

import scala.language.higherKinds
import scala.util.hashing.{
	Hashing,
	MurmurHash3
	}

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.time.types.ReadableInstant


/**
 * The '''EntityTag''' type defines the Domain Object Model concept of an
 * immutable [[https://tools.ietf.org/html/rfc7232#section-2.3 ETag]] for an
 * arbitrary [[com.github.osxhacker.foundation.models.core.entity.Entity]] type ''T''.  For
 * this system, a tag for ''T'' is defined in terms of its 
 * [[com.github.osxhacker.foundation.models.core.Identifier]] and when it was
 * `lastChanged`.
 *
 * @author osxhacker
 *
 */
final case class EntityTag (private val content : String)
{
	/**
	 * The externalized method produces a stringified representation of the
	 * '''EntityTag''' capable of being transmitted to an arbitrary external
	 * process.
	 */
	def externalized[M[_]] (implicit A : Applicative[M]) : M[String] =
		A.point (content);
}


object EntityTag
{
	/// Class Imports
	import scalaz.Id._
	import scalaz.std.string._
	import scalaz.syntax.equal._

	
	/// Instance Properties
	private val algorithm = MurmurHash3.orderedHashing;


	/**
	 * The apply method produces an '''EntityTag''' from hashing both an
	 * [[com.github.osxhacker.foundation.models.core.entity.EntityRef]] and the
	 * [[com.github.osxhacker.foundation.models.core.time.types.ReadableInstant]] it was
	 * last changed.
	 */
	def apply[T <: Entity[T]] (
		ref : EntityRef[T],
		lastChanged : ReadableInstant
		)
		: EntityTag =
	{
		val hash = algorithm.hash (ref.externalized[Id] :: lastChanged :: Nil);

		return new EntityTag (hash.toHexString);
	}


	/// Implicit Conversions
	implicit val EntityTagShow : Show[EntityTag] =
		new Show[EntityTag] {
			override def shows (a : EntityTag) : String = s"tag=${a.content}";
			}


	implicit val EntityTagEqual : Equal[EntityTag] = Equal.equalA;
}

