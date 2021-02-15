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

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''DiscreteStatusSpec''' type defines the unit tests to certify the
 * [[com.github.osxhacker.foundation.models.core.DiscreteStatus]] abstraction.  It
 * also serves as an exemplar for how
 * [[com.github.osxhacker.foundation.models.core.DiscreteStatus]]'s can be defined
 * and used within the system.
 *
 * @author osxhacker
 */
class DiscreteStatusSpec
	extends ProjectSpec
{
	/// Class Imports
	/**
	 * '''SampleStatus''' is a good representative for how '''DiscreteStatus'''
	 * types are defined.  They are pretty simple and most of the time may have
	 * behaviour only defined in '''DiscreteStatus'''.
	 */
	sealed abstract class SampleStatus (override val name : String)
		extends DiscreteStatus[SampleStatus]
	
	/**
	 * The companion '''SampleStatus''' `object` is defined so that the
	 * implicits provided by
	 * [[com.github.osxhacker.foundation.models.core.DiscreteStatusImplicits]] are
	 * available.
	 */
	object SampleStatus
		extends DiscreteStatusImplicits[SampleStatus]

	case object SampleActive
		extends SampleStatus ("Active")
	
	case object SampleInactive
		extends SampleStatus ("Inactive")
	
	
	/// Class Types
	
	
	/// Instance Properties
	
	"A DiscreteStatus type" must {
		"be a model of Equals" in {
			assert (SampleActive.asInstanceOf[Equals].asInstanceOf[AnyRef] ne null);
			}

		"have value equality" in {
			assert (SampleActive !== SampleInactive);
			}
		
		"support the Equal typeclass" in {
			assertCompiles ("""
				Equal[SampleStatus].equal (
					SampleActive,
					SampleInactive
					)
				""");
			}

		"support Equal typeclass syntax" in {
			assertCompiles ("""
				import Scalaz._

				SampleActive === SampleInactive;
				""");
			}

		"support mapping over instances" in {
			import std.anyVal._
			
			assert (
				SampleActive.liftMap (SampleActive) (-\/ (1), \/-(0))
				=== -\/ (1)
				);
			}
		
		"support pattern matching instances" in {
			assert {
				(SampleInactive.asInstanceOf[AnyRef]) match {
					case SampleActive () => false;
					case _ => true;
					}
				}

			assert {
				(SampleInactive.asInstanceOf[AnyRef]) match {
					case SampleInactive () => true;
					case _ => false;
					}
				}
			}
		
		"support pattern matching string content" in {
			assert {
				"Active" match {
					case SampleActive () => true;
					case _ => false;
					}
				}
			}
		
		"allow for monadic filtering using 'when'" in {
			import Scalaz._
			
			val status : SampleStatus = SampleActive;
			
			assert (status.when[Option] (SampleActive) ("x").isDefined);
			assert (status.when[Option] (SampleInactive) ("x").isEmpty);
			}
		
		"allow for monadic filtering using 'unless'" in {
			import Scalaz._
			
			val status : SampleStatus = SampleActive;
			
			assert (status.unless[Option] (SampleActive) (123).isEmpty);
			assert (status.unless[Option] (SampleInactive) (123).isDefined);
			}
		}
}

