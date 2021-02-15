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

package com.github.osxhacker.foundation.services.osgi

import scala.language.postfixOps

import org.osgi.framework.Bundle
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Specification


/**
 * The '''BundleSpecification''' type defines the category of possible
 * [[org.osgi.framework.Bundle]]-based
 * [[com.github.osxhacker.foundation.models.core.Specification]]s available for use
 * in [[com.github.osxhacker.foundation.services.osgi.scenario]]s.
 *
 * @author osxhacker
 *
 */
sealed abstract class BundleSpecification ()
	extends Specification[Bundle]


/**
 * The '''ContainsEntries''' is the
 * [[com.github.osxhacker.foundation.services.osgi.BundleSpecification]] determines if
 * a '''bundle''' has one or more entries which match the `filePattern`,
 * optionally only within a `path` (or under `path` if `recurse` is `true`).
 *
 * @author osxhacker
 *
 */
final case class ContainsEntries (
	private val filePattern : String,
	private val path : Option[String] = None,
	private val recurse : Boolean = true
	)
	extends BundleSpecification
{
	/// Class Imports
	import std.string._
	import syntax.all._
	import syntax.std.option._


	override def apply (bundle : Bundle) : Boolean =
	{
		val enumeration = bundle.findEntries (
			path orZero,
			filePattern,
			recurse
			);

		return (enumeration ne null) && enumeration.hasMoreElements ();
	}
}


/**
 * The '''ExcludingBundle''' is the
 * [[com.github.osxhacker.foundation.services.osgi.BundleSpecification]] which "filters
 * out" a '''bundle''' if it is the same as `exclude`.
 *
 * @author osxhacker
 *
 */
final case class ExcludingBundle (private val exclude : Bundle)
	extends BundleSpecification
{
	override def apply (bundle : Bundle) : Boolean = exclude != bundle;
}


/**
 * The '''IsActiveBundle''' is the
 * [[com.github.osxhacker.foundation.services.osgi.BundleSpecification]] which determines
 * if an arbitrary '''bundle''' is __currently__ in an active state.
 *
 * @author osxhacker
 *
 */
final case class IsActiveBundle ()
	extends BundleSpecification
{
	override def apply (bundle : Bundle) : Boolean =
		(bundle.getState () & Bundle.ACTIVE) != 0;
}


/**
 * The '''IsApplicationBundle''' is the
 * [[com.github.osxhacker.foundation.services.osgi.BundleSpecification]] which determines
 * if an arbitrary '''bundle''' is a product of `com.github.osxhacker.foundation`.
 *
 * @author osxhacker
 *
 */
final case class IsApplicationBundle ()
	extends BundleSpecification
{
	override def apply (bundle : Bundle) : Boolean =
		bundle.getSymbolicName.startsWith ("com.github.osxhacker.foundation");
}


/**
 * The '''IsStartingBundle''' is the
 * [[com.github.osxhacker.foundation.services.osgi.BundleSpecification]] which determines
 * if an arbitrary '''bundle''' is __currently__ in an active state.
 *
 * @author osxhacker
 *
 */
final case class IsStartingBundle ()
	extends BundleSpecification
{
	override def apply (bundle : Bundle) : Boolean =
		(bundle.getState () & Bundle.STARTING) != 0;
}
