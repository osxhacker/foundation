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

package com.github.osxhacker.foundation.models.core.functional

import scala.reflect.ClassTag

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}


/**
 * The '''Tristate''' `object` defines the behaviour expected of a
 * [[com.github.osxhacker.foundation.models.core.functional.TristateT]] specialized with
 * an `Option`.
 *
 * @author osxhacker
 *
 */
object Tristate
	extends TristateFunctions
{
	/// Class Imports
	import TristateT.tristateT


	def apply[A] (ooa : Option[Option[A]]) : Tristate[A] = tristateT (ooa);
}


trait TristateFunctions
{
	/// Class Imports
	import TristateT.{
		absentT,
		presentT
		}

	import syntax.std.boolean._


	def absent[A] () : Tristate[A] = absentT[Option, A] ();


	def empty[A] () : Tristate[A] = presentT[Option, A] (None);


	/**
	 * The mkTristate method is a model of the FACTORY pattern and will mint
	 * a '''Tristate'''.  If '''p''' is `true`, the '''Tristate''' will be
	 * present (yet may still be empty).
	 */
	def mkTristate[A] (p : Boolean)
		(f : => Option[A])
		: Tristate[A] =
		Tristate (p.option (f));


	def present[A] (a : A) : Tristate[A] = presentT (Option (a));


	def unapply[T] (that : Any)
		(implicit ct : ClassTag[T])
		: Option[T] =
		that match {
			case t : Tristate[T] =>
				t.run.getOrElse (None);

			case _ =>
				None;
			}
}

