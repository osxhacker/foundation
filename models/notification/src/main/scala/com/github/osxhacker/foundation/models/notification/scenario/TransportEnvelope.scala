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

import javax.mail._
import javax.mail.internet._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Deadline

import akka.actor.ActorSystem
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.akkax.ActorSystemAware
import com.github.osxhacker.foundation.models.core.error._
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.core.time
import com.github.osxhacker.foundation.models.notification.akkax.extension.MailTransportPool
import com.github.osxhacker.foundation.models.notification.email.Envelope


/**
 * The '''TransportEnvelope''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] which will attempt to transmit
 * a [[javax.mail.Message]] to an SMTP server.  For details about the contents
 * of the produced [[javax.mail.internet.MimeMultipart]] used or the ordering
 * of "body parts", see
 * [[https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html the spec]].
 * 
 * Note that the `apply` method first lifts the
 * [[com.github.osxhacker.foundation.models.notification.email.Envelope]] into the
 * ''Future'' monad so that the `implicit` ''ExecutionContext'' is used when
 * the potentially blocking
 * [[com.github.osxhacker.foundation.models.notification.akkax.extension.MailTransportPool]]
 * is used.
 *
 * @author osxhacker
 *
 */
final case class TransportEnvelope (private val source : Envelope)
	(
		implicit override val system : ActorSystem,
		private val ec : ExecutionContext,
		private val d : Deadline
	)
	extends Scenario[FutureEither[Message]]
		with ActorSystemAware
{
	/// Class Imports
	import functional.futures._
	import functional.futures.monad._
	import syntax.all._
	import syntax.std.option._
	import time.statics.Instant


	override def apply () : FutureEither[Message] =
		source.point[FutureEither] >>= send;


	private def addBody (message : Message, envelope : Envelope) : Message =
		envelope.html.cata (
			addHtmlBody (message, envelope.text),
			addTextBody (message, envelope.text)
			);


	private def addHtmlBody (message : Message, text : String)
		: String => Message =
		html => {
			val multipart = new MimeMultipart ("alternative");
			val (htmlPart, textPart) = (new MimeBodyPart (), new MimeBodyPart ());

			htmlPart.setContent (html, "text/html");
			htmlPart.setContentID ("html-body");
			textPart.setText (text);
			textPart.setContentID ("text-body");

			multipart.addBodyPart (textPart);
			multipart.addBodyPart (htmlPart);
			message.setContent (multipart);

			message;
			}


	private def addTextBody (message : Message, text : String) : Message =
	{
		message.setText (text);

		return message;
	}


	private def createMessage (session : Session, envelope : Envelope)
		: ApplicationError \/ Message =
		(
			envelope.from.toInternetAddress () |@|
			envelope.recipient.toInternetAddress ()
		) {
		case (from, to) =>
			val message = new MimeMessage (session);

			// TODO: what about "reply to"?
			message.setFrom (from);
			message.setRecipients (
				Message.RecipientType.TO,
				Array[Address] (to)
				);

			message.setSubject (envelope.subject);
			message.setSentDate (Instant.now ().toDate ());
			addBody (message, envelope);

			message;
		}


	private def send (envelope : Envelope) : FutureEither[Message] =
		FutureEither {
			MailTransportPool (system) {
				case (session, transport) =>
					createMessage (session, envelope).map {
						message =>

						message.saveChanges ();
						transport.sendMessage (
							message,
							message.getAllRecipients ()
							);

						message;
						}
				}
				.flatMap (ea => ea);	/// unwind nested \/'s
			}
}

