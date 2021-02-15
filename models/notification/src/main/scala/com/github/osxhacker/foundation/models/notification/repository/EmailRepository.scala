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

package com.github.osxhacker.foundation.models.notification.repository

import akka.NotUsed
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.entity.Repository
import com.github.osxhacker.foundation.models.notification.EmailAddress
import com.github.osxhacker.foundation.models.notification.email._


/**
 * The '''EmailRepository''' trait defines the contract for interacting with a
 * persistent store such that the persistent representation of
 * [[com.github.osxhacker.foundation.models.notification.email.Email]] instances are
 * managed.
 *
 * @author osxhacker
 *
 */
trait EmailRepository
	extends Repository[Email]
{
	/**
	 * The create method will produce an
	 * [[com.github.osxhacker.foundation.models.notification.email.Email]] which is
	 * [[com.github.osxhacker.foundation.models.notification.email.DeliveryIsPending]]
	 * in the persistent store.  Note that multiple invocations with the same
	 * '''envelope''' will produce multiple
	 * [[com.github.osxhacker.foundation.models.notification.email.Email]] instances.
	 */
	def create (envelope : Envelope) : Source[Email, NotUsed];


	/**
	 * This query method attempts to resolve all
	 * [[com.github.osxhacker.foundation.models.notification.email.Email]] instances which
	 * were intended for the given '''recipient'''.
	 */
	def query (recipient : EmailAddress) : Source[Email, NotUsed];


	/**
	 * This query method attempts to resolve all
	 * [[com.github.osxhacker.foundation.models.notification.email.Email]] instances which
	 * have the specified '''status'''.
	 */
	def query (status : DeliveryStatus) : Source[Email, NotUsed];
}
