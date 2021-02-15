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

import java.net.URL
import java.util.{
	Collections => JCollections,
	Dictionary => JDictionary,
	Enumeration => JEnumeration,
	HashMap => JHashMap,
	Hashtable => JHashtable,
	Map => JMap
	}

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

import org.osgi.framework.BundleContext
import org.osgi.service.cm.ConfigurationAdmin

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.typesafe.config.{
	Config,
	ConfigFactory,
	ConfigParseOptions
	}


import BundleConfiguration._
import syntax.all._


/**
 * The '''BundleConfiguration''' type defines the ability to produce
 * [[com.typesafe.config.Config]] instances which incorporate settings
 * from the
 * [[https://osgi.org/javadoc/r4v42/org/osgi/service/cm/ConfigurationAdmin.html OSGi ConfigAdmin]]
 * service.
 *
 * @author osxhacker
 */
final class BundleConfiguration (
	val path : String,
	val extras : JMap[String, AnyRef],
	private val context : BundleContext
	)
{
	/// Instance Properties
	@BeanProperty
	val config : Config = createUsing (context, path, extras);
	
	
	def this (context : BundleContext, path : String) = this (
		path,
		noExtras,
		context
		);
	
	
	def this (
		context : BundleContext,
		path : String,
		extras : JMap[String, AnyRef]
		)
		= this (path, extras, context);
	
	
	def this (
		context : BundleContext,
		path : String,
		configAdmin : ConfigurationAdmin,
		pid : String
		)
		= this (
			path,
			propertiesForPid (configAdmin, pid) |> convertToMap,
			context
			);
}


object BundleConfiguration
{
	/// Class Imports
	import Scalaz._


	/// Instance Properties
	private val noExtras : JMap[String, AnyRef] = new JHashMap[String, AnyRef];
	

	private def convertToMap (dictionary : JDictionary[String, AnyRef])
		: JMap[String, AnyRef] =
	{
		val jmap = new JHashMap[String, AnyRef] (dictionary.size);

		for (k <- dictionary.keys.asScala)
			jmap.put (k, dictionary.get (k));
		
		return jmap;
	}

		
	private def createUsing (
		context : BundleContext,
		path : String,
		extras : JMap[String, AnyRef]
		)
		: Config =
	{
		val loader = new ClassLoader () {
			override def getResources (name : String) : JEnumeration[URL] =
				asJavaEnumeration (
					context.getBundles.flatMap {
						bundle =>

						(
							Option (bundle.getResources (name)) |
							JCollections.emptyEnumeration[URL] ()
						).asScala;
						}
					.toIterator
					);
			}

		val parseOptions = ConfigParseOptions.defaults ()
			.setClassLoader (loader);

		return ConfigFactory.parseMap (extras)
			.withFallback (
				ConfigFactory.parseResources (
					getClass.getClassLoader,
					path,
					parseOptions
					)
				)
			.withFallback (ConfigFactory.load (parseOptions));
	}


	/**
	 * An OSGi container may provide `null` references when attempting to
	 * resolve configuration for a `pid`.  So this method defensively tries
	 * to provide the `JDictionary` for the `pid` if one is present, and an
	 * empty `JDictionary` if not.
	 */
	private def propertiesForPid (
		configAdmin : ConfigurationAdmin,
		pid : String
		)
		: JDictionary[String, AnyRef] =
		(
			Option (pid).filter (!_.isEmpty) >>=
			(p => configAdmin.getConfiguration (p).point[Option]) >>=
			(c => Option (c.getProperties))
		) | new JHashtable[String, AnyRef];
}

