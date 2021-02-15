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

package com.github.osxhacker.foundation.models.core.error

import java.io.{
	PrintWriter,
	StringWriter
	}

import scala.language.postfixOps


/**
 * The '''ApplicationError''' type serves as the common ancestor for ''all''
 * Foundation application errors emitted by the bundles.
 *
 * @author osxhacker
 */
abstract class ApplicationError (
	val message : String,
	val cause : Option[Throwable] = None
	)
	extends Throwable (message, cause orNull)
{
	/**
	 * This version of the toString method includes the concrete
	 * '''ApplicationError''' type name, the provided `message`, and the
	 * `cause` as a stack trace if one has been provided.
	 */
	override def toString () : String =
		"""%s: message="%s" cause=%s""".format (
			getClass,
			message,
			cause.map {
				nested =>

				val writer = new StringWriter ();

				writer.append ("\n");
				nested.printStackTrace (new PrintWriter (writer));

				writer.toString ();
				}
			);
}

