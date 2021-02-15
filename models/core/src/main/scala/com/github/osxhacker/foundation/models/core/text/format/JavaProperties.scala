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

import java.util.Properties

import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.text.encoding.Base64


/**
 * The '''JavaProperties''' type defines the
 * [[com.github.osxhacker.foundation.models.core.text.format.Protocol]] responsible for
 * producing [[java.util.Properties]] compatible representations of run-time
 * types.
 *
 * @author osxhacker
 *
 */
sealed abstract class JavaProperties
	extends Protocol
{
	/// Class Imports
	import JavaProperties._


	/// Class Types
	override type Repr = String


	override def createValue[A] (value : A) : Value =
		value match {
			case ba : Array[Byte] =>
				Value (Base64[Array[Byte]] ().encode (ba));
				
			case other : Any =>
				Value (other.toString);
			}


	override def emit (node : Tree) : String =
		interpret (node, Seq.empty);


	private def interpret (node : Tree, parents : Seq[Symbol]) : String =
		node match {
			case Value (content) =>
				parents map (_.name) mkString ("", ".", "=" + content + "\n");

			case Node (children) =>
				children.foldLeft ("") {
					case (accum, child) =>
						
					accum + interpret (child._2, parents :+ child._1);
					}
				
			case _ : Tree =>
				""
			}
}


object JavaProperties
{
	def apply[A] (instance : A)
		(implicit formatter : Formatter[JavaProperties, A], p : JavaProperties)
		: String =
		p.emit (formatter.write (instance) (p));


	/// Implicit Conversions
	implicit val protocol : JavaProperties = new JavaProperties () {};
}
