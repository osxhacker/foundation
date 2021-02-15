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

package com.github.osxhacker.foundation.models.notification.akkax.extension

import javax.mail.{
	Transport,
	Session
	}

import java.util.Properties

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.akkax.ActorSystemAware
import com.github.osxhacker.foundation.models.core.akkax.extension.{
	ResourcePool,
	SystemWideMaterializer
	}

import com.github.osxhacker.foundation.models.core.error._
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.net
import com.github.osxhacker.foundation.models.notification.NotificationSettings
import com.github.osxhacker.foundation.models.security.password.SystemPassword
import com.github.osxhacker.foundation.models.security.scenario.LookupSystemPassword


/**
 * The '''MailTransportPool''' type defines an [[akka.actor.Extension]] which
 * manages a [[com.github.osxhacker.foundation.models.core.akkax.extension.ResourcePool]]
 * of `[[javax.mail.Session]] -> [[javax.mail.Transport]]` pairs.
 *
 * @author osxhacker
 *
 */
final class MailTransportPool (
	private val smtp : SystemPassword,
	private val settings : NotificationSettings
	)
	(implicit override val system : ActorSystem)
	extends ResourcePool[(Session, Transport)] (
		min = settings.email.transport.poolMin,
		max = settings.email.transport.poolMax
		)
		with ActorSystemAware
{
	/// Class Imports
	import \/.fromTryCatchThrowable
	import syntax.std.boolean._
	import syntax.std.option._
	import system.dispatcher


	/// Class Types
	protected class TransportFactory ()
		extends ResourceFactory
	{
		/// Class Imports
		import syntax.all._


		override def create () : (Session, Transport) =
			session.map {
				s =>

				s -> s.getTransport ("smtp");
				}
			.valueOr (e => throw e);


		override def activate (pair : (Session, Transport)) : Unit =
			pair._2.isConnected ().unless {
				(
					smtp.host |@|
					smtp.account
				) {
					case (host, account) =>
						pair._2.connect (host, account, smtp.content);
					}
				}


		override def destroy (pair : (Session, Transport)) : Unit =
			pair._2.close ();
	}


	/// Instance Properties
	override protected lazy val factory = new TransportFactory ();

	private val session = createSession ();


	private def createSession () : ApplicationError \/ Session =
		for {
			host <- smtp.host
			port <- smtp.port \/> ConfigurationError ("missing SMTP port")
			session <- fromTryCatchThrowable[Session, Throwable] {
				val props = new Properties ();

				props.put ("mail.smtp.host", host);
				props.put ("mail.smtp.port", port.toString ());
				props.put ("mail.smtp.starttls.enable", "true");
				props.put ("mail.smtp.ssl.trust", "*");

				Session.getInstance (props);
				}
				.leftMap (failed)

			} yield session;


	private def failed : Throwable => ApplicationError =
		cause => InitializationError (
			"creating JavaMail Session",
			Option (cause)
			);
}


object MailTransportPool
	extends ExtensionId[MailTransportPool]
		with ExtensionIdProvider
{
	/// Class Imports
	import akkax.streams._
	import functional.futures._
	import functional.futures.comonad._
	import syntax.comonad._


	override def createExtension (system : ExtendedActorSystem)
		: MailTransportPool =
	{
		import system.dispatcher

		val settings = NotificationSettings (system);

		implicit val deadline = settings.loadingTimeout fromNow;
		implicit val mat = SystemWideMaterializer (system).materializer;
		implicit val s : ActorSystem = system;

		val password = Source.single (settings.email.transport.password)
			.via (LookupSystemPassword (settings.loadingTimeout).run ())
			.toFutureEither
			.run
			.copoint
			.valueOr (e => throw e);

		return new MailTransportPool (password, settings);
	}


	override def lookup = this;
}
