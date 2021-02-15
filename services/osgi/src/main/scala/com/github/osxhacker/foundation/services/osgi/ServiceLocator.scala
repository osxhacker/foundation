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

import scala.collection.JavaConverters
import scala.language.{
	higherKinds,
	postfixOps
	}

import scala.reflect.ClassTag

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import org.osgi.framework.{
	BundleContext,
	FrameworkUtil
	}


/**
 * The '''ServiceLocator''' type provides the ability to resolve OSGi services
 * at run-time.  This is often used to either discover optional services based
 * on deployment or to break circular dependencies by way of lazy resolution.
 *
 * @author osxhacker
 */
class ServiceLocator[T <: AnyRef : ClassTag] ()
{
	/// Class Imports
	import JavaConverters._
	import std.list._
	import syntax.monoid._
	import syntax.std.option._


	/// Instance Properties
	private lazy val bundleContext : Option[BundleContext] =
		Option (FrameworkUtil.getBundle (getClass)) map ( _.getBundleContext);


	/**
	 * The services method attempts to resolve ''all'' OSGi services
	 * registered for the interface type `T`.
	 */
	def services () : Seq[T] = resolve (spec = None);


	/**
	 * The servicesMatching method uses an optional '''filter''' to resolve
	 * all ''currently registered'' OSGi services of type `T`.  The syntax
	 * for a '''filter''' is defined as
	 * [[https://tools.ietf.org/html/rfc1960 LDAP Search Filters]].
	 */
	def servicesMatching (spec : FilterSpecification[T])
		: Seq[T] =
		resolve (Some (spec));


	private def resolve (spec : Option[FilterSpecification[T]])
		: Seq[T] =
	{
		val currentServices = bundleContext map {
			context =>
				
			val service =
				implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]];

			val references =
				context.getServiceReferences (
					service,
					spec map (_.toFilter) orNull
					).asScala;
			
			references.toList
				.map (context.getService[T])
				.filterNot (_ eq null)
				.filter (r => spec.map (_.isSatisfiedBy (r)) | true);
			}
		
		return currentServices orZero;
	}
}
