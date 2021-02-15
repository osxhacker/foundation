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

package com.github.osxhacker.foundation

import sbt._
import sbt.Keys._

import com.typesafe.sbt.osgi.SbtOsgi
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys


/**
 * The '''OSGiBundle''' `trait` defines the build settings for the
 * `osgi` [[sbt.Project]], which is provides utility logic for defining
 * OSGi bundles
 *
 * @author osxhacker
 */
trait OSGiBundle
{
	/// Self Type Constraints
	this : Models =>


	def defaultSettings : Seq[Setting[_]]


	/// Instance Properties
	lazy val osgi = Project (
		id = "services-osgi",
		base = file ("services/osgi"),
		dependencies = Seq (
			core % "compile->compile;test->test"
			),

		settings = defaultSettings ++
			OSGiBundle.OSGiSettings.osgi ++
			Seq (libraryDependencies ++= OSGiBundle.Dependencies.osgi)
		)
		.enablePlugins (SbtOsgi);
}


object OSGiBundle
{
	/// Class Types
	object Dependencies
	{
		lazy val osgi = Models.compile ++ Models.test ++ Seq (
			"com.github.domino-osgi" %% "domino" % "1.1.5"
			);
	}


	object OSGiSettings
		extends OSGiSupport
	{
		/// Instance Properties
		private val basePackage = "com.github.osxhacker.foundation.services";

		val osgi = simple (basePackage, ".osgi");

		val declareImports : Seq[Setting[_]] =
			projectImports (basePackage + ".osgi" :: Nil);
	}
}

