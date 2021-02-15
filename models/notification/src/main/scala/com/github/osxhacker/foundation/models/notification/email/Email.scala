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

import java.util.{
	List => JList
	}

import scala.collection.JavaConverters
import scala.language.{
	higherKinds,
	postfixOps
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.entity._
import com.github.osxhacker.foundation.models.core.error._
import com.github.osxhacker.foundation.models.core.net.Scheme
import com.github.osxhacker.foundation.models.core.text.Explanation
import com.github.osxhacker.foundation.models.notification.EmailAddress
import com.github.osxhacker.foundation.models.security.vault.VaultAware


/**
 * The '''Email''' type defines the Domain Object Model reification of an
 * electronic mail communique which originates from the system.  It is an
 * [[com.github.osxhacker.foundation.models.core.entity.Entity]] whose identity is
 * established without using the properties which constitute an '''Email'''.
 *
 * @author osxhacker
 *
 */
abstract class Email ()
	extends Entity[Email]
		with EntityStatus[Email, DeliveryStatus]
		with VaultAware[Email]
{
	/// Class Imports
	import State._
	import std.boolean._
	import std.option._
	import syntax.all._
	import syntax.std.all._


	/// Class Types
	override type EntityType <: Email


	protected trait EmailLenses
		extends EntityLenses
			with StatusLens
	{
		def attempts : EntityType @> Int;
		def deliveryReceipt : EntityType @> Option[Receipt];
		def failureReason : EntityType @> Option[Explanation];
		def from : EntityType @> EmailAddress;
		def html : EntityType @> Option[String];
		def recipient : EntityType @> EmailAddress;
		def subject : EntityType @> String;
		def text : EntityType @> String;
	}


	/// Instance Properties
	lazy val toView : Email.View = new Email.View (this);

	override protected val lenses : EmailLenses;


	/// Constructor Body
	import lenses._


	/**
	 * The createEnvelope method attempts to produce an
	 * [[com.github.osxhacker.foundation.models.notification.email.Envelope]] from
	 * '''this''' instance if the current `status` is
	 * [[com.github.osxhacker.foundation.models.notification.email.DeliveryIsInProgress]],
	 * failing the operation if not.
	 */
	def createEnvelope () : ApplicationError \/ Envelope =
	{
		val builder = for {
			to <- recipient.st
			f <- from.st
			subj <- subject.st
			t <- text.st
			h <- html.st
			} yield Envelope (
				recipient = to,
				from = f,
				subject = subj,
				text = t,
				html = h
				);

		return (status.get (self) === DeliveryIsInProgress).fold (
			builder.eval (self).right,
			InvalidModelStateError (
				"email must have a status of '%s' and not '%s'".format (
					DeliveryIsInProgress.name,
					status.get (self).name
					)
				).left
			);
	}


	/**
	 * The ensure method behaves similar to a "higher-ordered filter" and
	 * will lift '''this''' instance into the `\/` monad iff the provided
	 * '''filter''' evaluates to `true`.  The '''error''' is provided
	 * otherwise.
	 */
	def ensure[A <: ApplicationError] (error : => A)
		(filter : Email => Boolean)
		: A \/ Email =
		filter (this) either (this) or (error);


	/**
	 * The isPending method indicates to the caller whether or not '''this'''
	 * instance can be `sent`.
	 */
	def isPending () : Boolean = status.get (self) === DeliveryIsPending;


	/**
	 * The isRetryable method indicates whether or not '''this''' instance
	 * is capable of being transmission retried.
	 */
	def isRetryable (maximum : Int) : Boolean =
		whenAttemptsAllow[Option, Boolean] (maximum) (
			status.get (self) === DeliveryIsPending ||
			status.get (self) === DeliveryIsRejected
			) | false;


	/**
	 * The reject method attempts to transition '''this''' instance into a
	 * [[com.github.osxhacker.foundation.models.notification.email.DeliveryIsRejected]]
	 * status, failing if the current `status` does not support that
	 * transition.
	 */
	def reject (reason : Explanation) : ValidatedChange[Email] =
		whenStatusOf[ValidatedChange] (DeliveryIsInProgress) {
			for {
				_ <- changeStatus (DeliveryIsRejected)
				_ <- deliveryReceipt := None
				_ <- failureReason := Some (reason)
				updated <- get
				} yield updated;
			}


	/**
	 * The retry method attempts to transition '''this''' instance to a
	 * [[com.github.osxhacker.foundation.models.notification.email.DeliveryIsPending]]
	 * status, failing if the `status` is currently
	 * [[com.github.osxhacker.foundation.models.notification.email.DeliveryIsComplete]].
	 */
	def retry () : ValidatedChange[Email] =
		unlessStatusOf[ValidatedChange] (DeliveryIsComplete) {
			for {
				_ <- changeStatus (DeliveryIsPending)
				_ <- deliveryReceipt := None
				_ <- failureReason := None
				updated <- get
				} yield updated;
			}


	/**
	 * The sent method indicates that '''this''' instance has been successfully
	 * communicated to its `recipient`.  Required for this is for the
	 * collaborator to provided a
	 * [[com.github.osxhacker.foundation.models.notification.email.Receipt]] produced from
	 * an [[com.github.osxhacker.foundation.models.notification.email.EmailNotification]]
	 * implementation.
	 */
	def sent (receipt : Receipt) : ValidatedChange[Email] =
		whenStatusOf[ValidatedChange] (DeliveryIsInProgress) {
			for {
				_ <- changeStatus (DeliveryIsComplete)
				_ <- deliveryReceipt := Some (receipt)
				_ <- failureReason := None
				updated <- get
				} yield updated;
			}


	/**
	 * The transmitting method indicates that '''this''' instance is
	 * __currently__ being attempted to deliver to an
	 * [[https://en.wikipedia.org/wiki/Message_transfer_agent MTA]].
	 * If successful, the returned instance can transition using either
	 * `reject` or `sent`.
	 */
	def transmitting () : ValidatedChange[Email] =
		whenStatusOf[ValidatedChange] (DeliveryIsPending) {
			for {
				_ <- changeStatus (DeliveryIsInProgress)
				_ <- attempts %== (_ + 1)
				_ <- deliveryReceipt := None
				_ <- failureReason := None
				updated <- get
				} yield updated;
			}


	/**
	 * The whenAttemptsReach method is a higher-order function which will
	 * produce an `M[A]` iff `attempts` is less than the '''maximum'''
	 * specified.
	 */
	def whenAttemptsAllow[M[_], A] (maximum : Int)
		(a : => A)
		(implicit mp : MonadPlus[M])
		: M[A] =
		(attempts.get (self) < maximum).guard[M] (a);


	/**
	 * The whenAttemptsReach method is a higher-order function which will
	 * produce an `M[A]` iff `attempts` is greater than or equal to
	 * '''maximum'''.
	 */
	def whenAttemptsReach[M[_], A] (maximum : Int)
		(a : => A)
		(implicit mp : MonadPlus[M])
		: M[A] =
		(attempts.get (self) >= maximum).guard[M] (a);
}


object Email
{
	/// Class Imports
	import syntax.equal._
	import syntax.std.boolean._


	/// Class Types
	sealed class StatusExtractor[A <: DeliveryStatus] (desired : A)
	{
		def unapply (email : Email) : Option[Email] =
			(email.toView.status () === desired).option (email);
	}


	case object IsComplete
		extends StatusExtractor (DeliveryIsComplete)


	case object IsInProgress
		extends StatusExtractor (DeliveryIsInProgress)


	case object IsRejected
		extends StatusExtractor (DeliveryIsRejected)


	case object IsPending
		extends StatusExtractor (DeliveryIsPending)


	/// Instance Properties
	implicit val scheme = Scheme[Email] ("email");


	/// Implicit Conversions
	implicit class View (private val email : Email)
		extends Entity.View (email)
			with EntityStatus.View[Email, DeliveryStatus]
	{
		/// Class Imports
		import JavaConverters._
		import email.{
			lenses,
			self
			}


		def attempts () : Int = lenses.attempts.get (self);
		def deliveryReceipt () : Option[Receipt] =
			lenses.deliveryReceipt.get (self);

		def failureReason () : Option[Explanation] =
			lenses.failureReason.get (self);

		def from () : EmailAddress = lenses.from.get (self);
		def html () : Option[String] = lenses.html.get (self);
		def recipient () : EmailAddress = lenses.recipient.get (self);
		def subject () : String = lenses.subject.get (self);
		def text () : String = lenses.text.get (self);


		/// Java Accessors
		def getAttempts () : Int = attempts ();
		def getDeliveryReceipt () : Receipt = deliveryReceipt () orNull;
		def getFailureReason () : Explanation = failureReason () orNull;
		def getFrom () : EmailAddress = from ();
		def getHtml () : String = html () orNull;
		def getRecipient () : EmailAddress = recipient ();
		def getSubject () : String = subject ();
		def getText () : String = text ();
	}
}
