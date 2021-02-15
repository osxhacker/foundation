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


/**
 * The '''Models''' `trait` defines the build settings for all
 * `com.github.osxhacker.foundation.models` bundles defined in the system.
 *
 * @author osxhacker
 */
trait Models
{
	def defaultSettings : Seq[Setting[_]]


	/// Instance Properties
	lazy val core = Project (
		id = "models-core",
		base = file ("models/core"),
		settings = defaultSettings ++
			Models.OSGiSettings.core ++
			Seq (
				libraryDependencies ++= Models.Dependencies.core
				)
		)
		.enablePlugins (SbtOsgi);

	lazy val notification = Project (
		id = "models-notification",
		base = file ("models/notification"),
		dependencies = Seq (
			core % "compile->compile;test->test",
			security % "compile->compile;test->test"
			),

		settings = defaultSettings ++
			Models.OSGiSettings.notification ++
			Seq (
				libraryDependencies ++= Models.Dependencies.notification
				)
		)
		.enablePlugins (SbtOsgi);

	lazy val security = Project (
		id = "models-security",
		base = file ("models/security"),
		dependencies = Seq (
			core % "compile->compile;test->test"
			),

		settings = defaultSettings ++
			Models.OSGiSettings.security ++
			Seq (
				libraryDependencies ++= Models.Dependencies.security
				)
		)
		.enablePlugins (SbtOsgi);

	lazy val allModels = Seq[ProjectReference] (
		core,
		notification,
		security
		);
	}


object Models
{
	object Dependencies
	{
		lazy val core = compile ++ test ++
			Seq (
				"org.apache.commons" % "commons-pool2" % "2.6.1"
				);

		lazy val notification = compile ++ test ++
			Seq (
				"com.sun.mail" % "javax.mail" % "1.6.2"
				);

		lazy val security = core ++ Akka.http;
	}


	object OSGiSettings
		extends OSGiSupport
	{
		/// Instance Properties
		private val basePackage = "com.github.osxhacker.foundation.models";

		val core = simple (basePackage, ".core");
		val notification = simple (
			basePackage,
			".notification",
			".core.*" ::
			".security.*" ::
			Nil
			);

		val security = simple (basePackage, ".security");

		def modelImports = projectImports (basePackage) (
			".core.*" ::
			".notification.*" ::
			".security.*" ::
			Nil
			);
		}


	/// Compile Dependencies
	def compile = Scalaz.core ++
		Akka.compile ++
		Seq (
			"com.chuusai" %% "shapeless" % "2.3.2",
			"com.github.nscala-time" %% "nscala-time" % "2.20.0" excludeAll (
				ExclusionRule ("joda-time", "joda-time"),
				ExclusionRule ("org.joda", "joda-convert")
				),

			"commons-codec" % "commons-codec" % "1.11",
			"io.dropwizard.metrics" % "metrics-core" % "4.0.2",
			"io.dropwizard.metrics" % "metrics-jmx" % "4.0.2",
			"io.dropwizard.metrics" % "metrics-jvm" % "4.0.2" % "runtime",
			"io.dropwizard.metrics" % "metrics-healthchecks" % "4.0.2",
			"joda-time" % "joda-time" % "2.10.5",
			"nl.grons" %% "metrics4-scala" % "4.0.1" excludeAll (
				ExclusionRule ("io.dropwizard.metrics", "metrics-core"),
				ExclusionRule ("io.dropwizard.metrics", "metrics-healthchecks")
				),

			"org.joda" % "joda-convert" % "2.2.1",
			"org.scala-lang" % "scala-compiler" % "2.12.10",
			"org.scala-lang" % "scala-library" % "2.12.10",
			"org.scala-lang" % "scala-reflect" % "2.12.10",
			"org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
			"org.scala-lang.modules" %% "scala-collection-compat" % "2.0.0",
			"org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
			"org.scala-lang.modules" %% "scala-xml" % "1.2.0",
			"org.osgi" % "org.osgi.core" % "6.0.0" % "provided"
			);

	/// Test Dependencies
	lazy val logback = Seq (
		"ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
		"ch.qos.logback" % "logback-core" % "1.2.3" % "runtime"
		);

	def test = Akka.test ++
		logback ++
		Seq (
			"org.scalatest" %% "scalatest" % "3.0.8" % "test"
			);
}

