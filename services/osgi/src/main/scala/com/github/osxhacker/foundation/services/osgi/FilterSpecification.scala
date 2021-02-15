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

import scala.language.{
	implicitConversions,
	postfixOps
	}

import scala.reflect.ClassTag

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core._


/**
 * The '''FilterSpecification''' type defines the category of possible
 * [[https://tools.ietf.org/html/rfc1960 LDAP Search Filters]] available to
 * use as
 * [[https://osgi.org/javadoc/r4v43/core/org/osgi/framework/Filter.html OSGi Filters]].
 *
 * @author osxhacker
 */
sealed abstract class FilterSpecification[T <: AnyRef : ClassTag]
	extends Specification[T]
{
	/// Instance Properties
	def property : Option[(String, String)] = None;
	def interface : Option[Class[_]] = None;
	def name : Option[String] = None;


	/// Instance Properties
	override lazy val toString = FilterSpecification.mkString (this);


	/**
	 * The apply method for '''FilterSpecification'''s simply ensure that the
	 * '''candidate''' is a valid `T` reference.  Fine-grained filtering is
	 * performed by the OSGi container which evaluates
	 * '''FilterSpecification'''s.
	 */
	override def apply (candidate : T) : Boolean = (candidate ne null);
}


object FilterSpecification
	extends FilterSpecificationImplicits
{
	/// Class Imports
	import Scalaz._


	/// Instance Properties
	private val ComponentNameKey = "osgi.service.blueprint.compname";
	private val ObjectClassKey = "objectClass";


	private[osgi] def buildFrom[T <: AnyRef : ClassTag] (
		spec : Specification[T]
		)
		: String =
		spec match {
			case AndSpecification (lhs, rhs) =>
				"&(%s)(%s)".format (
					buildFrom[T] (lhs),
					buildFrom[T] (rhs)
					);

			case NotSpecification (s) =>
				"!(%s)".format (buildFrom[T] (s));

			case OrSpecification (lhs, rhs) =>
				"|(%s)(%s)".format (
					buildFrom[T] (lhs),
					buildFrom[T] (rhs)
					);

			case fs : FilterSpecification[T] =>
				mkString (fs);
			}


	private def mkString[T <: AnyRef] (spec : FilterSpecification[T]) : String =
		(
			spec.interface.map (c => ObjectClassKey + "=" + c.getName) |+|
			spec.name.map (ComponentNameKey + "=" + _) |+|
			spec.property.map (p => p._1 + "=" + p._2)
		) orZero;
}


trait FilterSpecificationImplicits
{
	/// Implicit Conversions
	implicit class ToFilterString[T <: AnyRef : ClassTag] (
		spec : Specification[T]
		)
	{
		def toFilter () : String =
			"(%s)".format (FilterSpecification.buildFrom[T] (spec));
	}
}


/**
 * The '''ComponentName''' type is a
 * [[com.github.osxhacker.foundation.services.osgi.FilterSpecification]]
 * which is satisfied when the '''desired''' name matches the Blueprint
 * `component-name` attribute.
 * 
 * @author osxhacker
 */
final case class ComponentName[T <: AnyRef : ClassTag] (
	private val desired : String
	)
	extends FilterSpecification[T]
{
	/// Instance Properties
	override val name : Option[String] = Option (desired) map (_.trim);
}


/**
 * The '''ImplementsInterface''' type is a
 * [[com.github.osxhacker.foundation.services.osgi.FilterSpecification]]
 * which is satisfied when the type `T` implements `U`.
 * 
 * @author osxhacker
 */
final case class ImplementsInterface[
	T <: AnyRef with U : ClassTag,
	U : ClassTag
	] ()
	extends FilterSpecification[T]
{
	/// Instance Properties
	override val interface : Option[Class[_]] =
		Some (implicitly[ClassTag[U]].runtimeClass);
}


/**
 * The '''ServiceProperty''' type is a
 * [[com.github.osxhacker.foundation.services.osgi.FilterSpecification]]
 * which is satisfied when an arbitrary service of type `T` has a
 * `service-properties` `entry` matching the '''desired''' key/value.
 * 
 * @author osxhacker
 */
final case class ServiceProperty[T <: AnyRef : ClassTag] (
	private val desired : (String, Any)
	)
	extends FilterSpecification[T]
{
	/// Class Imports
	import Scalaz._


	/// Instance Properties
	override val property : Option[(String, String)] =
		Some (desired map (_.toString));
}

