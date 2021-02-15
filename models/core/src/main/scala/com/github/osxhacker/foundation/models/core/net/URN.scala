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

package com.github.osxhacker.foundation.models.core
package net

import scala.language.postfixOps

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * The '''URN''' type reifies the
 * [[http://tools.ietf.org/html/rfc2141 Uniform Resource Name]] concept by
 * specializing [[java.net.URI]] so that all '''URN''' instances conform to
 * the IETF specification.
 *
 * Each '''URN''' is composed of a "namespace identifier" (`nid`) and a
 * "namespace specific string" (`nss`) along with mandatory a "urn:" prefix.
 *
 * @author osxhacker
 */
final class URN (val nid : Scheme[AnyRef], val nss : String)
	extends Equals
		with Ordered[URN]
		with Serializable
{
	/// Instance Properties
	private val uri = new URI (nid.name, nss, null);


	override def canEqual (that : Any) : Boolean =
		that.asInstanceOf[URN] ne null;


	override def compare (other : URN) : Int = uri.compareTo (other.uri);


	override def equals (that : Any) : Boolean =
		canEqual (that) && uri.equals (that.asInstanceOf[URN].uri);


	override def hashCode : Int = uri.hashCode;


	override def toString : String = uri.toString
}


object URN
{
	/// Class Imports
	import std.option._
	import syntax.monad._
	import syntax.std.boolean._
	import syntax.std.option._
	import \/._


	/**
	 * Use this version of apply to create a '''URN''' when both the
	 * [[com.github.osxhacker.foundation.models.core.Scheme]] and scheme-specific
	 * portion are known.
	 */
	def apply[A] (scheme : Scheme[A], nss : String) : Throwable \/ URN =
	{
		val checkForEmpty : String => Throwable \/ String =
			s => Option (s).filterNot (_.isEmpty) \/>[Throwable] (
				new IllegalArgumentException ("Invalid URN NSS detected")
				);

		val buildUri : String => String =
			content => s"${scheme.name}:${content.toLowerCase}";

		return checkForEmpty (nss).map (buildUri) >>= (apply (_ : String));
	}


	/**
	 * This version of the apply method attempts to produce a '''URN''' from
	 * an arbitrary `String`, producing a [[java.lang.Throwable]] if either
	 * it is not a valid [[java.net.URI]] or does not conform to '''URN'''
	 * requirements.
	 */
	def apply (uri : String) : Throwable \/ URN =
		\/.fromTryCatchThrowable[URI, Throwable] (new URI (uri)) >>= {
			apply (_ : URI);
			}


	/**
	 * This version of the apply method attempts to create a '''URN''' if
	 * the given '''uri''' `isOpaque` and not `null`.
	 */
	def apply (uri : URI) : Throwable \/ URN =
	{
		val mustBeOpaque : URI => Throwable \/ URI =
			u => Option (u).filter (_.isOpaque) \/>[Throwable] (
				new IllegalArgumentException (
					s"Invalid URI detected for URN: '${uri}'"
					)
				);

		val maybeUnwrap : URI => Throwable \/ URN =
			uri => uri.getScheme.toLowerCase.equals ("urn").fold (
				apply (uri.getSchemeSpecificPart),
				\/- (
					new URN (
						Scheme[AnyRef] (uri.getScheme.toLowerCase),
						uri.getSchemeSpecificPart.toLowerCase
						)
					)
				);

		return mustBeOpaque (uri) >>= maybeUnwrap;
	}


	/**
	 * The '''URN''' extractor determines if a given '''candidate''' is a 
	 * [[http://tools.ietf.org/html/rfc2141 Uniform Resource Name]].
	 */
	def unapply (candidate : AnyRef) : Option[URN] =
		candidate match {
			case urn : URN =>
				Option (urn);

			case uri : URI =>
				apply (uri) toOption;

			case maybeUri : String =>
				apply (maybeUri) toOption;

			case _ =>
				None;
			}
}

