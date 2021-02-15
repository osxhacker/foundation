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

package com.github.osxhacker.foundation.models.security.password

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	ConfigurationError
	}

import com.github.osxhacker.foundation.models.core.functional.ErrorOr
import com.github.osxhacker.foundation.models.core.net


/**
 * The '''SystemPassword''' type defines the Domain Object Model concept of an
 * authentication challenge having to be provided to a collaborator.  Examples
 * of this are SMTP server accounts and strategic partner integration.
 *
 * The `logical` [[com.github.osxhacker.foundation.models.core.net.URI]] must
 * have the general form of:
 *
 * {{{
 *	val valid = "system-password://the-account@email.local/subsystem";
 * }}}
 *
 * Of the possible [[https://tools.ietf.org/html/rfc3986 hierarchical URI]]
 * properties, the following are made available by '''SystemPassword''':
 * 
 *   - '''user-info''': a required `account`.
 *   - '''host''': a required `host`.
 *   - '''path''': required and incorporated via `toLocation`.
 *   - '''port''': optional port number.
 * 
 * Note that '''SystemPassword''' properties are __not__ those used to resolve
 * the '''SystemPassword''' ''content'', but instead are associated with the
 * password information.  Conceptually, '''SystemPassword''' is most similar
 * to
 * [[https://www.freebsd.org/cgi/man.cgi?query=passwd&apropos=0&sektion=5&manpath=FreeBSD+12.1-RELEASE+and+Ports&arch=default&format=html passwd(5)]]
 *
 * @author osxhacker
 *
 * @see [[https://docs.oracle.com/javase/7/docs/api/java/net/URI.html]]
 * 
 */
final case class SystemPassword (val logical : net.URI, val content : String)
{
	/// Class Imports
	import syntax.equal._
	import syntax.std.option._
	import net.URI
	import net.uri._


	/// Instance Properties
	lazy val account : ErrorOr[String] =
		Option (logical.getUserInfo ()) \/> ConfigurationError (
			"missing user-info URI property for entry logical=%s".format (
				logical
				)
			);

	lazy val host : ErrorOr[String] =
		Option (logical.getHost ()) \/> ConfigurationError (
			"missing host URI property for entry logical=%s".format (
				logical
				)
			);

	lazy val port = Option (logical.getPort ()).filter (_ > 0);


	override def equals (that : Any) : Boolean =
		canEqual (that) &&
		logical === that.asInstanceOf[SystemPassword].logical;


	override def hashCode () : Int = logical.hashCode ();


	override def toString () : String =
		"SystemPassword(%s,%s)".format (logical, "X" * content.length);
}


object SystemPassword
{
	/// Class Imports
	import net.{
		Scheme,
		URI
		}


	/**
	 * This version of the apply method provides functional-style creation of a
	 * '''SystemPassword''' instance from a '''logical''' location and a
	 * [[com.github.osxhacker.foundation.models.security.password.CleartextPassword]]
	 * instance.
	 */
	def apply (logical : URI, clear : CleartextPassword) : SystemPassword =
		new SystemPassword (logical, clear.content);


	/// Instance Properties
	implicit val scheme : Scheme[SystemPassword] = Scheme ("system-password");
}

