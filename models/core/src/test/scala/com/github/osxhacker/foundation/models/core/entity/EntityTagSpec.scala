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

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core._
import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	DomainValueError
	}


/**
 * The '''EntityTagSpec''' type defines the specification which certifies the
 * [[com.github.osxhacker.foundation.models.core.entity.EntityTag]] for fitness of
 * purpose.
 *
 * @author osxhacker
 *
 */
final class EntityTagSpec
	extends ProjectSpec
{
	/// Class Imports
	import scalaz.Id._
	import scalaz.std.option._
	import scalaz.syntax.all._
	import scalaz.syntax.std.all._


	/// Class Types
	case class SampleEntity (
		val _id : Identifier,
		val _timestamps : Option[ModificationTimes] = None
		)
		extends Entity[SampleEntity]
	{
		/// Class Types
		override type EntityType = SampleEntity;
		
		
		/// Instance Properties
		override val lenses = new EntityLenses {
			override val id = lensFor[Identifier] (
				set = (e, v) => e.copy (_id = v),
				get = _._id
				);

			override val timestamps = plensFor[ModificationTimes] (
				set = e => Option (mt => e copy (_timestamps = Some (mt))),
				get = _._timestamps
				);
			}
			
		override val self = this;
	}


	/// Instance Properties
	private val tooLong = 64;
	private val savedEntity = Identifier ("urn:test:saved") map {
		id =>

		SampleEntity (id, ModificationTimes.now.pure[Option]);
		}

	private val unsavedEntity = Identifier ("urn:test:unsaved") map {
		id =>

		SampleEntity (id);
		}


	"An EventTag" must {
		"be producable from a 'saved' Entity" in {
			val result = savedEntity >>= defineTag;

			assert (result.isRight);
			result foreach {
				tag =>

				assert (tag.externalized[Option].isDefined);
				}
			}

		"not be produced from an 'unsaved' Entity" in {
			val result = unsavedEntity >>= defineTag;

			assert (result.isLeft);
			}
		
		"produce different tags for different Entity instances" in {
			val anotherSavedEntity = Identifier ("urn:test:save-#2") map {
				id =>

				SampleEntity (id, ModificationTimes.now.pure[Option]);
				}

			val result = for {
				a <- savedEntity >>= defineTag
				b <- anotherSavedEntity >>= defineTag
				} yield (a.externalized[Option], b.externalized[Option]);

			assert (result.isRight);
			result foreach {
				case (first, second) =>
					
				assert (first != second);
				}
			}
		}


	"An EventTag" should {
		"have a reasonably short representation" in {
			val result = savedEntity >>= defineTag;

			assert (result.isRight);
			result foreach {
				tag =>

				assert (tag.externalized[Id].length < tooLong);
				}
			}
		}
	
	
	private def defineTag (instance : SampleEntity)
		: ApplicationError \/ EntityTag =
		instance.toTag \/> DomainValueError ("failed to create a tag");
}
