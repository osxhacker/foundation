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

package com.github.osxhacker.foundation.models.notification.email

import akka.NotUsed
import akka.stream.scaladsl.Flow
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.entity.EntityRef
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither


/**
 * The '''EmailNotification''' trait defines the service contract responsible
 * for producing email destined for one or more
 * [[com.github.osxhacker.foundation.models.notification.EmailAddress]]es, as defined
 * within each [[com.github.osxhacker.foundation.models.notification.email.Envelope]].
 *
 * @author osxhacker
 *
 */
trait EmailNotification
{
	/**
	 * The redeliver method attempts to `send` all
	 * [[com.github.osxhacker.foundation.models.notification.email.Email]] known to the
	 * system which has not been successfully delivered __and__ still qualifies
	 * for delivery.
	 */
	def redeliver () : FutureEither[Vector[EntityRef[Email]]];


	/**
	 * The send method produces a [[akka.stream.scaladsl.Flow]] which will
	 * enqueue an [[com.github.osxhacker.foundation.models.notification.email.Envelope]]
	 * to be sent to its `recipient` by way of an SMTP server.  This may happen
	 * at any point after the [[akka.stream.scaladsl.Flow]] is executed.
	 */
	def send () : Flow[Envelope, EntityRef[Email], NotUsed];
}
