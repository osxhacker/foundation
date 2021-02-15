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

package com.github.osxhacker.foundation.models.core


/**
 * =Overview=
 * 
 * The '''time''' `package` centralizes the types concerned with representing
 * and calculating units of time within the system.  Since the JDK types
 * dealing with this concept are woefully inadequate, the
 * [[http://joda-time.sourceforge.net/userguide.html Joda-Time]] libarary is
 * used extensively, with
 * [[https://github.com/nscala-time/nscala-time nscala-time]] employed to make
 * using [[http://joda-time.sourceforge.net/userguide.html Joda-Time]] more
 * "Scala-esque".
 * 
 * ==Scalaz Support==
 * 
 * Neither [[http://joda-time.sourceforge.net/userguide.html Joda-Time]] nor
 * [[https://github.com/nscala-time/nscala-time nscala-time]] are "aware" of
 * Scalaz, so applicable type classes are defined here in order to smoothly
 * support Scalaz.
 * 
 * ==Type Insulation==
 * 
 * One of the goals of this `package` is to insulate the rest of the system
 * from the concrete types exposed by 
 * [[http://joda-time.sourceforge.net/userguide.html Joda-Time]] and
 * [[https://github.com/nscala-time/nscala-time nscala-time]].  Doing so
 * facilitates migration to later versions and/or other implementations,
 * such as [[https://jcp.org/en/jsr/detail?id=310 JSR-310]].
 *
 * @author osxhacker
 */
package object time
{
	/// Class Types
	object all
		extends Implicits
			with Statics
			with Types
	
	
	object implicits
		extends Implicits


	object statics
		extends Statics


	object types
		extends Types
	
	
	/// Instance Properties
	
}
