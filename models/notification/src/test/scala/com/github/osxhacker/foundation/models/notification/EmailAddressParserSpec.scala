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

package com.github.osxhacker.foundation.models.notification

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import org.scalatest.prop.TableDrivenPropertyChecks


/**
 * The '''EmailAddressParserSpec''' type defines the unit tests which certify
 * [[com.github.osxhacker.foundation.models.notification.EmailAddressParser]] for
 * fitness of purpose.
 *
 * @author osxhacker
 *
 */
final class EmailAddressParserSpec
	extends ProjectSpec
		with TableDrivenPropertyChecks
		with EmailAddressParser
{
	/// Instance Properties
	val invalidAddresses = Table (
		"Invalid email addresses",
		"b o b @ e x a m p l e . c o m",
		" ",
		"bob",
		"bob@",
		"@domain",
		"remote!somebody@university.edu",
		"spammer%hijacked@example.edu",
		"bob@example@alice@example.com"
		);
	
	val validAddresses = Table (
		"Valid email addresses",
		"bob@example.com",
		" bob@example.com",
		"\tbob@example.com",
		" \tbob@example.com",
		"bob@example.com ",
		"bob@example.com\t",
		"bob@example.com\t ",
		"\t bob@example.com\t ",
		"root@localhost",
		"root@127.0.0.1"
		);
	
	
	"The EmailAddressParser" must {
		"detect invalid addresses" in {
			forAll (invalidAddresses) {
				candidate =>

				assert (parseEmailAddresss (candidate).isLeft);
				}
			}
		
		"allow valid addresses" in {
			forAll (validAddresses) {
				candidate =>

				val result = parseEmailAddresss (candidate);
				
				assert (result.isRight);
				assert (result.forall (_._1.isEmpty == false));
				assert (result.forall (_._2.isEmpty == false));
				}
			}
		}
}
