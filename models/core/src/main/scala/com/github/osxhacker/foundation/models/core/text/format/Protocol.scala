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

import scala.language.higherKinds

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''Protocol''' trait defines the domain of text formats, or
 * "protocols", arbitrary types can be represented in through
 * [[com.github.osxhacker.foundation.models.core.text.format.Formatter]] transformation.
 *
 * @author osxhacker
 *
 */
trait Protocol
{
	/// Class Imports


	/// Class Types
	type Repr
	
	
	sealed trait Tree
	
	case class Collection (items : Seq[Tree])
		extends Tree

	case class Node (children : List[(Symbol, Tree)])
		extends Tree

	case class Value (content : String)
		extends Tree
		
	case object Empty
		extends Tree


	def createValue[A] (value : A) : Value;
	
	
	/**
	 * The emit method must produce a suitable '''Repr''' for the symbolic
	 * '''Tree''' produced by
	 * [[com.github.osxhacker.foundation.models.core.text.format.Formatter]].
	 */
	def emit (root : Tree) : Repr;
}
