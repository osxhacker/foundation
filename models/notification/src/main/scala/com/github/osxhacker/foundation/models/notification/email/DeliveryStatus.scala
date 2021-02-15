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

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	DiscreteStatus,
	DiscreteStatusImplicits
	}


/**
 * The '''DeliveryStatus''' trait defines the Domain Object Model type
 * reifying each distinct [[com.github.osxhacker.foundation.models.core.DiscreteStatus]]
 * an [[com.github.osxhacker.foundation.models.notification.email.Email]] can have.
 *
 * @author osxhacker
 *
 */
sealed abstract class DeliveryStatus (override val name : String)
	extends DiscreteStatus[DeliveryStatus]


object DeliveryStatus
	extends DiscreteStatusImplicits[DeliveryStatus]
{
	def unapply (candidate : Any) : Option[DeliveryStatus] =
		candidate match {
			case DeliveryIsComplete () =>
				Some (DeliveryIsComplete);

			case DeliveryIsInProgress () =>
				Some (DeliveryIsInProgress);

			case DeliveryIsPending () =>
				Some (DeliveryIsPending);

			case DeliveryIsRejected () =>
				Some (DeliveryIsRejected);

			case _ =>
				None;
			}
}


case object DeliveryIsComplete
	extends DeliveryStatus ("Complete")


case object DeliveryIsInProgress
	extends DeliveryStatus ("InProgress")


case object DeliveryIsPending
	extends DeliveryStatus ("Pending")


case object DeliveryIsRejected
	extends DeliveryStatus ("Rejected")
