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

import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys


/**
 * The '''ProjectBuild''' `object` defines the
 * [[http://www.scala-sbt.org/release/docs sbt]] build settings for the
 * Foundation project.
 *
 * Inspiration for the structure was drawn from the AkkaBuild.scala code
 * within akka/project.  Many thanks go to the people involved in that!
 *
 * @author	osxhacker
 *
 */
object ProjectBuild
	extends Build
		with Models
		with OSGiBundle
{
	/// Instance Properties
	val systemName = "foundation";

	lazy val buildSettings = Seq (
		organization := "com.github.osxhacker.foundation",
		version := BuildProperties ("version"),
		scalaVersion := "2.12.10"
		);

	/// sbt settings applicable to any build
	lazy val defaultSettings = Seq (
		scalacOptions in Compile ++= Seq (
			"-encoding", "UTF-8",
			"-target:jvm-1.8",
			"-deprecation",
			"-feature",
			"-unchecked",
			"-Xlog-reflective-calls",
			"-Xlint:-unused,_",
			"-Ywarn-unused:-imports,_"
			),

		EclipseKeys.eclipseOutput := Some (".target"),

		/// Ensure output is not as choppy
		logBuffered in Test := false,

		/// As of sbt v0.13.17, "cleanFiles" is no longer used.  So add the
		/// Eclipse files to the files produced by cleanFilesTask
		cleanFilesTask := {
			cleanFilesTask.value ++ Vector (
				file (baseDirectory.value + "/.cache-main"),
				file (baseDirectory.value + "/.cache-tests"),
				file (baseDirectory.value + "/.classpath"),
				file (baseDirectory.value + "/.project"),
				file (baseDirectory.value + "/.settings"),
				file (baseDirectory.value + "/.target")
				)
			}
		) ++
		addCommandAlias ("gen-eclipse", ";package;eclipse");

	override lazy val settings = super.settings ++ buildSettings;

	/// Top-level project
	lazy val root = (project in file ("."))
		.aggregate (allModels ++ Seq[ProjectReference] (osgi) : _*)
		.settings (defaultSettings);

		/*
	Project (
		id = systemName,
		base = file ("."),
		aggregate = allModels ++ Seq[ProjectReference] (osgi),
		settings = defaultSettings
		);
		*/
}

