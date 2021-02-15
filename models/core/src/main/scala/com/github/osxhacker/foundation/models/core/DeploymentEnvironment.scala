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

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import error.{
	ApplicationError,
	DomainValueError
	}

import net.{
	Scheme,
	URN
	}


/**
 * The '''DeploymentEnvironment''' type defines the Domain Object Model
 * concept of a distinct system deployment, such as "development" or "QA".
 * Predefined '''DeploymentEnvironment'''s are provided in the companion
 * object, though these do not represent a closed set of all possible
 * environments.
 *
 * @author osxhacker
 *
 */
final case class DeploymentEnvironment (val id : Identifier)
{
	/// Class Imports
	import syntax.all._
	import syntax.std.boolean._


	/// Instance Properties
	def name : String = id.withoutScheme ().toLowerCase ();


	/**
	 * The when method produces an ''A'' iff '''other''' is logically the
	 * same as '''this''' instance.
	 */
	def when[A] (other : DeploymentEnvironment)
		(a : => A)
		: Option[A] =
		(id === other.id).option (a);


	/**
	 * The whenNot method produces an ''A'' iff '''other''' is logically
	 * disequal with '''this''' instance.
	 */
	def whenNot[A] (other : DeploymentEnvironment)
		(a : => A)
		: Option[A] =
		(id =/= other.id).option (a);
}


object DeploymentEnvironment
{
	/// Class Imports
	import syntax.all._
	import syntax.std.boolean._


	/// Instance Properties
	val scheme = Scheme[DeploymentEnvironment] ("deployment");

	val Development = DeploymentEnvironment (
		new Identifier (new URN (scheme, "development"))
		);

	val Production = DeploymentEnvironment (
		new Identifier (new URN (scheme, "production"))
		);

	val QA = DeploymentEnvironment (
		new Identifier (new URN (scheme, "qa"))
		);

	val UnitTest = DeploymentEnvironment (
		new Identifier (new URN (scheme, "unit-test"))
		);


	/**
	 * This version of the apply method is provided to support functional style
	 * creation from an externalized `String` '''value'''.
	 */
	def apply (value : String)
		: ApplicationError \/ DeploymentEnvironment =
		Identifier (value) map {
			id =>

			new DeploymentEnvironment (id)
			}


	/**
	 * This version of the apply method is provided to support functional style
	 * creation from a '''uri''' which __must__ be a
	 * [[com.github.osxhacker.foundation.models.core.net.URN]] as well as have a matching
	 * [[com.github.osxhacker.foundation.models.core.net.Scheme]].
	 */
	def apply (uri : URI) : ApplicationError \/ DeploymentEnvironment =
		(URN (uri) >>= (apply _)).leftMap {
			e =>

			DomainValueError ("invalid uri", e);
			}


	/**
	 * This version of the apply method is provided to support functional style
	 * creation from a '''urn''' which __must__ have a matching
	 * [[com.github.osxhacker.foundation.models.core.net.Scheme]].
	 */
	def apply (urn : URN) : ApplicationError \/ DeploymentEnvironment =
		(urn.nid === scheme).fold (
			new DeploymentEnvironment (new Identifier (urn)).right,
			DomainValueError ("invalid scheme").left
			);


	/// Implicit Conversions
	implicit val DeploymentEnvironmentEqual : Equal[DeploymentEnvironment] =
		Equal.equalA;

	implicit val DeploymentEnvironmentShow : Show[DeploymentEnvironment] =
		Show.showFromToString;
}

