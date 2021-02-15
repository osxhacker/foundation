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

import scala.language.postfixOps

import org.scalatest.DiagrammedAssertions
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Identifier
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional.ErrorOr
import com.github.osxhacker.foundation.models.core.text.Explanation
import com.github.osxhacker.foundation.models.core.text.encoding.Base64
import com.github.osxhacker.foundation.models.notification.{
	EmailAddress,
	ProjectSpec
	}

import com.github.osxhacker.foundation.models.security.hash.MD5



/**
 * The '''EmailSpec''' type defines the unit-tests which certify the
 * [[com.github.osxhacker.foundation.models.notification.email.Email]] type for fitness
 * of purpose and serves as an exemplar for its use.
 *
 * @author osxhacker
 *
 */
final class EmailSpec
	extends ProjectSpec
		with DiagrammedAssertions
		with EmailNotificationSupport
{
	/// Class Imports
	import std.option._
	import syntax.all._


	/// Instance Properties
	val initial = createEmail ("test-1", DeliveryIsPending);
	val rejection = {
		val id = Identifier (Explanation.scheme, "test-error");

		Explanation (id orThrow);
		}


	"The Email Entity" must {
		"be a model of EntityStatus" in {
			val result = initial map (_.status);

			assert (result.isRight);
			}

		"support status extractors" in {
			initial match {
				case \/- (Email.IsPending (email)) =>
					assert (email.recipient ne null);

				case other =>
					fail (other.toString);
				}

			(initial >>= (_.transmitting ())) match {
				case \/- (Email.IsInProgress (email)) =>
					assert (email.recipient ne null);

				case other =>
					fail (other.toString);
				}
			}

		"disallow envelope creation when not transmitting" in {
			val result = initial >>= (_.createEnvelope ());

			assert (result.isLeft);
			}

		"be able to create an Envelope" in {
			val envelope = initial >>=
				(_.transmitting ()) >>=
				(_.createEnvelope ());

			assert (envelope.isRight);
			assert (envelope.map (_.recipient) == initial.map (_.recipient));
			}

		"enforce maximum attempts" in {
			val failedOnce = initial >>=
				(_.transmitting ()) >>=
				(_.reject (rejection));

			val failedTwice = failedOnce >>=
				(_.retry ()) >>=
				(_.transmitting ()) >>=
				(_.reject (rejection));

			assert (failedOnce.isRight);
			assert (failedTwice.isRight);

			val allowed = failedOnce map {
				_.whenAttemptsAllow[Option, String] (2) ("produced");
				}

			assert (allowed.isRight);
			assert (allowed.exists (_.isDefined));

			val disallowed = failedTwice map {
				_.whenAttemptsAllow[Option, String] (2) ("produced");
				}

			assert (disallowed.isRight);
			assert (disallowed.exists (_.isEmpty));
			}
		}


	private def createEmail (idContent : String, status : DeliveryStatus)
		: ApplicationError \/ SampleEmail =
		Identifier (Email.scheme, idContent) map {
			id =>

			SampleEmail (
				_id = id,
				_status = status,
				_recipient = EmailAddress[MD5, Base64] (
					"bob",
					"example.com"
					),

				_from = EmailAddress[MD5, Base64] (
					"alice",
					"example.com"
					),

				_attempts = 0,
				_deliveryReceipt = None,
				_failureReason = None,
				_subject = "Unit Test",
				_html = Some (
					"""|<html>
					|<body>
					|<h1>Beep</h1>
					|</body>
					|</html>
					|""".stripMargin
					),

				_text = "This is just a test",
				_timestamps = None
				);
			}
}
