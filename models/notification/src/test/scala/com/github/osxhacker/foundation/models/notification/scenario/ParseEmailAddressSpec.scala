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

package com.github.osxhacker.foundation.models.notification.scenario

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.notification.ProjectSpec


/**
 * The '''ParseEmailAddressSpec''' type defines the unit tests which certify
 * [[com.github.osxhacker.foundation.models.notification.scenario.ParseEmailAddress]]
 * for fitness of purpose and serve as an exemplar for its use.
 *
 * @author osxhacker
 *
 */
final class ParseEmailAddressSpec
	extends ProjectSpec
{
	"The ParseEmailAddress scenario" must {
		"reject missing email addresses" in {
			assert (ParseEmailAddress ("") ().isLeft);
			}
		
		"reject invalid email addresses" in {
			assert (ParseEmailAddress ("b\u0002ad@example.com") ().isLeft);
			}
		
		"allow valid email addresses" in {
			val result = ParseEmailAddress ("bob@example.com") ();
			
			assert (result.isRight, result.toString);
			result foreach {
				address =>

				assert (address.hash.isEmpty === false);
				}
			}
		}
}
