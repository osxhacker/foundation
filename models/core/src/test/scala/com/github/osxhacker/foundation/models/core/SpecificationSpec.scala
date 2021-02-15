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
 * The '''SpecificationSpec''' type serves as an exemplar for how the
 * [[com.github.osxhacker.foundation.models.core.Specification]] family of types
 * can be used within the system.
 *
 * @author osxhacker
 */
class SpecificationSpec
	extends ProjectSpec
{
	/// Class Imports
	
	
	/// Class Types
	case class NameAndAge (name : String, age : Int)
	

	case class WithinAgeRange (range : Range)
		extends Specification[NameAndAge]
	{
		override val toString = "within age range specification";
		
		
		override def apply (candidate: NameAndAge) : Boolean =
			range.contains (candidate.age);
	}
	
	
	/// Instance Properties
	private val people = NameAndAge ("bob", 24) ::
		NameAndAge ("alice", 21) ::
		NameAndAge ("mary", 18) ::
		Nil;
	

	"A Specification" must {
		"be usable as a predicate" in {
			val over21 = people.filter (WithinAgeRange (21 to 65));
			
			assert (over21.length < people.length);
			}

		"support anonymous functors" in {
			val spec = Specification ((c : NameAndAge) => !c.name.isEmpty);
			
			assert (people.forall (spec));
			}

		"support predicate composition" in {
			val over50orUnder21 = WithinAgeRange (51 to 65) ||
				WithinAgeRange (1 to 18);
			
			assert (people.exists (over50orUnder21));
			}
		
		"support AST decomposition" in {
			val over50orUnder21 = WithinAgeRange (51 to 65) ||
				WithinAgeRange (1 to 18);
			
			over50orUnder21 match {
				case OrSpecification (_, _) =>
					succeed;
					
				case other =>
					fail (other.toString);
				}
			}
		}
}
