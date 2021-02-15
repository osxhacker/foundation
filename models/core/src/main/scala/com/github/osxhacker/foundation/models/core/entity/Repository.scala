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

import scala.concurrent.Future

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.scaladsl.{
	Flow,
	Sink,
	Source
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}


/**
 * The '''Repository''' type defines the minimal Domain Object Model contract
 * for being able to `save` and `find`
 * [[com.github.osxhacker.foundation.models.core.entity.Entity]]s in a persistent
 * store.
 * 
 * ==Implementation Notes==
 * 
 * ''Every'' derived '''Repository''' type is expected to have additional
 * methods introduced in ''their'' contracts.  Also,
 * [[com.github.osxhacker.foundation.models.core.entity.Entity]] creation is the
 * responsiblity of '''Repository''' types as they must ensure the existence
 * of an arbitrary `A` beyond the run-time of the system.
 * 
 * '''Repository''' contracts are expected to have `query` methods defined
 * which produce [[akka.stream.scaladsl.Source]]s.  This is to support
 * streaming large result sets in an efficient, "reactive", manner.
 * 
 * @author osxhacker
 */
trait Repository[A <: Entity[A]]
{
	/**
	 * The delete method produces a deferred [[akka.stream.scaladsl.Sink]]
	 * which will remove the persistent representation of an ''A'' up to the
	 * implementation defined boundary for the type.
	 */
	def delete () : Sink[A, Future[IOResult]];


	/**
	 * The find method resolves an `A` for the given '''ref''', producing an
	 * error if one (and only one) is not found.
	 */
	def find (ref : EntityRef[A]) : Source[A, NotUsed];
	
	
	/**
	 * The findTag method attempts to resolve an
	 * [[com.github.osxhacker.foundation.models.core.entity.EntityTag]] corrsponding to
	 * the given '''ref''', failing if one (and only one) is not found.
	 */
	def findTag (ref : EntityRef[A]) : Source[EntityTag, NotUsed];
	
	
	/**
	 * The save method persists an '''entity''', creating it if needed.
	 */
	def save () : Flow[A, A, NotUsed];
}

