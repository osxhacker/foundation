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

package com.github.osxhacker.foundation.models.core.akkax

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.postfixOps

import com.typesafe.config.Config
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	DeploymentEnvironment,
	Identifier
	}

import com.github.osxhacker.foundation.models.core.error.ConfigurationError
import com.github.osxhacker.foundation.models.core.net.URI


/**
 * The '''SettingsAware''' trait declares the dependency on an `implicit`
 * '''settings''' [[com.github.osxhacker.foundation.models.core.akkax.Settings]] instance
 * being available.
 *
 * @author osxhacker
 *
 */
trait SettingsAware[A <: Settings]
{
	/// Instance Properties
	implicit def settings : A;
}


/**
 * The '''Settings''' type defines the common behaviour for all Akka-related
 * configuration settings types.  Since each subsystem is free to have their
 * own configuration section, there is no reason to "share" one subsystem's
 * '''Settings'''-based type with another subystem.
 *
 * @author osxhacker
 *
 */
abstract class Settings (val subsystem : String)
{
	/// Class Imports
	import syntax.id._
	import syntax.std.boolean._
	import syntax.std.option._


	/// Instance Properties
	/**
	 * All Foundation configuration entries *must* reside within the rootName
	 * path.
	 */
	val rootName = "foundation";

	/**
	 * The concrete type must provide the config property and, as such, it
	 * along with ''any'' property within '''Settings''' (not the concrete
	 * type) which uses it either must be a `lazy val` or a `def`.
	 */
	def config : Config;

	/**
	 * All application configurations *must* indicate what their
	 * [[com.github.osxhacker.foundation.models.core.DeploymentEnvironment]]
	 * is with an entry of `foundation.deployment`.
	 */
	lazy val deployment =
		DeploymentEnvironment (topLevelKey ("deployment") |> asURI);

	protected val asBoolean : String => Boolean =
		path => config.getBoolean (path);

	protected val asDuration : String => FiniteDuration =
		path => config.getDuration (path, MILLISECONDS) milliseconds;

	protected val asInt : String => Int =
		path => config.getInt (path);

	protected val asString : String => String =
		path => config.getString (path);
		
	protected val asURI : String => URI =
		path => new URI (config.getString (path));

	protected val asStringList : String => List[String] =
		path => asScalaBuffer (config.getStringList (path)).toList;

	protected val asURIList : String => List[URI] =
		path => asScalaBuffer (config.getStringList (path)).map (asURI).toList;

	protected val asMaybeBoolean : String => Option[Boolean] =
		path => config.hasPath (path).option (asBoolean (path));

	protected val asMaybeDuration : String => Option[FiniteDuration] =
		path => config.hasPath (path).option (asDuration (path));

	protected val asMaybeInt : String => Option[Int] =
		path => config.hasPath (path).option (asInt (path));

	protected val asMaybeString : String => Option[String] =
		path => config.hasPath (path).option (asString (path));
		
	protected val asMaybeURI : String => Option[URI] =
		path => config.hasPath (path).option (asURI (path));


	/**
	 * The find method searches for the "most specific" configuration setting
	 * identified by the given '''subpath''', searching "up" to the top level
	 * section.  For example:
	 * 
	 * {{{
	 * 	val rootName = "foo";
	 * 	val subsystem = "bar.baz";
	 * 
	 * 	/// This call will search for "entry" in the following order:
	 * 	/// foo.bar.baz.nested.entry
	 * 	/// foo.bar.baz.entry
	 * 	/// foo.bar.entry
	 * 	/// foo.entry
	 * 	val path = find ("nested.entry");
	 * }}}
	 */
	protected def find (subpath : String) : Option[String] =
	{
		val segments = s"$rootName.$subsystem.$subpath".split ('.');
		val (parents, entry) = (segments.init.inits.toList, segments.last);

		return parents.filterNot (_.isEmpty)
			.map (_ :+ entry)
			.map (_.mkString ("."))
			.find (config.hasPath);
	}


	/**
	 * The findOrDefault method searches for the "most specific" configuration
	 * setting identified by the given '''subpath''', searching "up" to the top
	 * level section.  If one is not found, fall back to
	 * `"default-" + subpath`.  For example:
	 * 
	 * {{{
	 * 	val rootName = "foo";
	 * 	val subsystem = "bar.baz";
	 * 
	 * 	/// This call will search for "entry" in the following order:
	 * 	/// foo.bar.baz.nested.entry
	 * 	/// foo.bar.baz.entry
	 * 	/// foo.bar.entry
	 * 	/// foo.entry
	 * 	/// foo.default-entry
	 * 	val path = findOrDefault ("nested.entry");
	 * }}}
	 */
	protected def findOrDefault (subpath : String) : String =
		find (subpath) | topLevelKey ("default-" + subpath.split ('.').last);


	/**
	 * The key method provides a "fully qualified" `Config` path to use for
	 * resolving a configurable property ''within the subsystem'' configuration
	 * section.
	 */
	protected def key (subpath : String) : String =
		s"$rootName.$subsystem.$subpath";


	/**
	 * Similar to the `key` method, the topLevelKey allows the caller to create
	 * a `Config` path for a particular configuration entry.  Where it differs
	 * is that the path can be anywhere within the `rootName` section.
	 */
	protected def topLevelKey (subpath : String) : String =
		s"$rootName.$subpath";
}


/**
 * The '''DefaultDurationSettingsAware''' type allows concrete
 * [[com.github.osxhacker.foundation.models.core.akkax.Settings]] types to
 * resolve [[scala.concurrent.duration.FiniteDuration]] values either in a
 * specific configuration location or using a top-level default.
 * 
 * @author osxhacker
 * 
 */
trait DefaultDurationSettingsAware
{
	/// Self Type Constraints
	this : Settings =>


	/// Class Imports
	import syntax.id._
	import syntax.std.option._


	/**
	 * The durationOrDefault method attempt to resolve '''subpath''' and, if
	 * it does not exist, will search enclosing scopes, falling back to use
	 * `"default-' + leaf` if one is not found.
	 */
	protected def durationOrDefault (subpath : String) : FiniteDuration =
		findOrDefault (subpath) |> asDuration;
}

