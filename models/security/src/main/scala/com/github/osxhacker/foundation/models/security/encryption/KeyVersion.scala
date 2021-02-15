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

package com.github.osxhacker.foundation.models.security.encryption

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''KeyVersion''' type defines a VALUE OBJECT which reifies the concept
 * of a distinct version number of a
 * [[com.github.osxhacker.foundation.models.security.encryption.CipherKeys]] instance.
 * 
 * '''KeyVersion''' is also a model of a [[scalaz.Monoid]], adhering to the
 * monoid laws and semigroup laws required of all [[scalaz.Monoid]]s.  The
 * laws are enfored here by:
 * 
 * {{{
 * 	val M = implicitly[Monoid[KeyVersion]];
 * 
 * 	assert (M.zero == KeyVersion (0));
 * 	assert (M.append (KeyVersion (5), M.zero) == KeyVersion (5));
 * 	assert (M.zero, M.append (KeyVersion (5)) == KeyVersion (5));
 *
 * 	assert ((KeyVersion (99) |+| KeyVersion (3)) == KeyVersion (99));
 * }}}
 *
 * @author osxhacker
 *
 */
final case class KeyVersion (val version : Int)
	extends Ordered[KeyVersion]
{
	override def compare (other : KeyVersion) : Int = version - other.version;


	/**
	 * The next method produces a new '''KeyVersion''' with a `version` one
	 * higher than '''this'''.
	 */
	def next () : KeyVersion = KeyVersion (version + 1);
}


object KeyVersion
{
	/// Instance Properties
	val empty : KeyVersion = KeyVersion (0);


	/// Implicit Conversions
	implicit val KeyVersionEqual : Equal[KeyVersion] = Equal.equalA;
	
	implicit val KeyVersionMonoid : Monoid[KeyVersion] =
		new Monoid[KeyVersion] {
			override val zero : KeyVersion = KeyVersion.empty;

			override def append (a : KeyVersion, b : => KeyVersion)
				: KeyVersion =
				KeyVersion (a.version.max (b.version));
			}

	implicit val KeyVersionShow : Show[KeyVersion] = Show.showFromToString;
}
