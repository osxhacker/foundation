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

import org.scalatest._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Identifier
import com.github.osxhacker.foundation.models.core.net.Scheme


/**
 * The '''EntitySupport''' trait defines supporting types and logic used to
 * certify [[com.github.osxhacker.foundation.models.core.entity.Entiy]]-based unit tests.
 *
 * @author osxhacker
 *
 */
trait EntitySupport
	extends SuiteMixin
{
	/// Self Type Constraints
	this : Suite =>


	/// Class Imports


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
	
	
	object SampleEntity
	{
		/// Instance Properties
		val scheme = Scheme[SampleEntity] ("sample");
	}
	
	
	/// Instance Properties
	
}
