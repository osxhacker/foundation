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

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

import akka.NotUsed
import akka.stream.scaladsl._
import org.scalatest.Suite
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Identifier
import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.entity.{
	EntityRef,
	MockRepository,
	ModificationTimes
	}

import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.core.scenario.GenerateUniqueIdentifier
import com.github.osxhacker.foundation.models.core.text.Explanation
import com.github.osxhacker.foundation.models.core.time
import com.github.osxhacker.foundation.models.notification.EmailAddress
import com.github.osxhacker.foundation.models.notification.repository.EmailRepository
import com.github.osxhacker.foundation.models.security.encryption.Algorithm
import com.github.osxhacker.foundation.models.security.vault.Vault


/**
 * The '''EmailNotificationSupport''' trait defines supporting types involved
 * in tests which depend upon an
 * [[com.github.osxhacker.foundation.models.notification.email.EmailNotification]] being
 * available.
 *
 * @author osxhacker
 *
 */
trait EmailNotificationSupport
{
	/// Self Type Constraints
	this : Suite =>


	/// Class Imports
	import akkax.streams._
	import syntax.equal._


	/// Class Types
	class MockEmailNotification ()
		extends EmailNotification
	{
		/// Instance Properties
		private val counter = new AtomicInteger ();
		private val generateId = GenerateUniqueIdentifier (Receipt.scheme);


		def reset () : Unit = counter.set (0);


		override def redeliver () : FutureEither[Vector[EntityRef[Email]]] = ???


		override def send () : Flow[Envelope, EntityRef[Email], NotUsed] =
			Flow.fromFunction {
				_ : Envelope =>

				generateId () map {
					id =>

					counter.incrementAndGet ();
					EntityRef[Email] (id);
					}
				}
				.valueOrFail ()


		def sent () : Int = counter.intValue ();
	}


	class MockEmailRepository (private val initial : Seq[Email] = Nil)
		extends MockRepository[Email] (initial)
			with EmailRepository
	{
		/// Class Imports
		import State._


		/// Instance Properties
		private val generateId = GenerateUniqueIdentifier (Email.scheme);


		override def create (envelope : Envelope) : Source[Email, NotUsed] =
		{
			val email : Email = SampleEmail (
				_id = generateId () valueOr (e => throw e),
				_status = DeliveryIsPending,
				_recipient = envelope.recipient,
				_from = envelope.from,
				_attempts = 0,
				_deliveryReceipt = None,
				_failureReason = None,
				_subject = envelope.subject,
				_html = envelope.html,
				_text = envelope.text,
				_timestamps = None
				);

			return withStore {
				for {
					_ <- init
					_ <- modify[StorageType] (_ + (email.toRef -> email))
					} yield Source.single (email);
				}
		}


		override def query (recipient : EmailAddress) : Source[Email, NotUsed] =
			withStore {
				for {
					existing <- init
					} yield Source (existing.values.toVector).filter {
						_.recipient == recipient;
						}
				}


		override def query (status : DeliveryStatus) : Source[Email, NotUsed] =
			withStore {
				for {
					existing <- init
					} yield Source (existing.values.toVector).filter {
						_.status == status;
						}
				}
	}


	/**
	 * The '''SampleEmail''' type serves as an example for how concrete
	 * [[com.github.osxhacker.foundation.models.notification.email.Email]]'s can be defined.
	 * Note that the properties are ''intentionally'' prefixed with
	 * an underscore so that each [[scalaz.Lens]] can be a `val` instead of a
	 * `def`.  Doing so avoids a "recursive value needs type" compilation
	 * error as well as exemplifies an idiom to help relieve GC pressure.
	 * 
	 * Another point to consider is that the primary constructor's parameters
	 * are marked as `private`.  The reasoning for this is simple: all
	 * interaction with a
	 * [[com.github.osxhacker.foundation.models.notification.email.Email]] is done via
	 * [[scalaz.Lens]]es.  This is to be expected for any
	 * [[com.github.osxhacker.foundation.models.core.Entity]].
	 */
	final case class SampleEmail (
		val _id : Identifier,
		val _status : DeliveryStatus,
		val _recipient : EmailAddress,
		val _from : EmailAddress,
		val _attempts : Int,
		val _deliveryReceipt : Option[Receipt],
		val _failureReason : Option[Explanation],
		val _subject : String,
		val _html : Option[String],
		val _text : String,
		val _timestamps : Option[ModificationTimes]
		)
		extends Email
	{
		/// Class Types
		override type EntityType = SampleEmail


		/// Instance Properties
		override val lenses = new EmailLenses {
			override val id = lensFor[Identifier] (
				set = (e, v) => e.copy (_id = v),
				get = _._id
				);

			override val attempts = lensFor[Int] (
				set = (e, v) => e.copy (_attempts = v),
				get = _._attempts
				);

			override val deliveryReceipt = lensFor[Option[Receipt]] (
				set = (e, v) => e.copy (_deliveryReceipt = v),
				get = _._deliveryReceipt
				);

			override val failureReason = lensFor[Option[Explanation]] (
				set = (e, v) => e.copy (_failureReason = v),
				get = _._failureReason
				);

			override val from = lensFor[EmailAddress] (
				set = (e, v) => e.copy (_from = v),
				get = _._from
				);

			override val html = lensFor[Option[String]] (
				set = (e, v) => e.copy (_html = v),
				get = _._html
				);

			override val recipient = lensFor[EmailAddress] (
				set = (e, v) => e.copy (_recipient = v),
				get = _._recipient
				);

			override val status = lensFor[DeliveryStatus] (
				set = (e, v) => e.copy (_status = v),
				get = _._status
				);

			override val subject = lensFor[String] (
				set = (e, v) => e.copy (_subject = v),
				get = _._subject
				);

			override val text = lensFor[String] (
				set = (e, v) => e.copy (_text = v),
				get = _._text
				);

			override val timestamps = plensFor[ModificationTimes] (
				set = e => Option (mt => e copy (_timestamps = Some (mt))),
				get = _._timestamps
				);
			}

		override val self = this;


		override def unvault[AlgoT <: Algorithm[AlgoT]] (vault : Vault[AlgoT])
			: ApplicationError \/ Email =
			vault.retrieve (this);


		override def vault[AlgoT <: Algorithm[AlgoT]] (vault : Vault[AlgoT])
			: ApplicationError \/ Email =
			vault.store (this);
	}
}
