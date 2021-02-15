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

import com.typesafe.sbt.osgi.{
	OsgiKeys,
	SbtOsgi
	}

import SbtOsgi.autoImport._


/**
 * The '''OSGiSupport''' `trait` provides OSGi build support for all
 * '''Project'''s defined in the system.
 *
 * @author osxhacker
 */
trait OSGiSupport
{
	/// Instance Properties
	def akkaImports = additionalImports (
		Seq (
			"akka;version=\"[2.5,2.6)\"",
			"akka.actor;version=\"[2.5,2.6)\"",
			"akka.actor.dsl;version=\"[2.5,2.6)\"",
			"akka.actor.dungeon;version=\"[2.5,2.6)\"",
			"akka.actor.setup;version=\"[2.5,2.6)\"",
			"akka.annotation;version=\"[2.5,2.6)\"",
			"akka.cluster;version=\"[2.5,2.6)\"",
			"akka.cluster.protobuf;version=\"[2.5,2.6)\"",
			"akka.cluster.protobuf.msg;version=\"[2.5,2.6)\"",
			"akka.cluster.routing;version=\"[2.5,2.6)\"",
			"akka.dispatch;version=\"[2.5,2.6)\"",
			"akka.dispatch.sysmsg;version=\"[2.5,2.6)\"",
			"akka.event;version=\"[2.5,2.6)\"",
			"akka.event.japi;version=\"[2.5,2.6)\"",
			"akka.io;version=\"[2.5,2.6)\"",
			"akka.japi;version=\"[2.5,2.6)\"",
			"akka.japi.function;version=\"[2.5,2.6)\"",
			"akka.japi.pf;version=\"[2.5,2.6)\"",
			"akka.japi.tuple;version=\"[2.5,2.6)\"",
			"akka.osgi;version=\"[2.5,2.6)\"",
			"akka.pattern;version=\"[2.5,2.6)\"",
			"akka.pattern.extended;version=\"[2.5,2.6)\"",
			"akka.remote;version=\"[2.5,2.6)\"",
			"akka.remote.artery;version=\"[2.5,2.6)\"",
			"akka.remote.artery.compress;version=\"[2.5,2.6)\"",
			"akka.remote.routing;version=\"[2.5,2.6)\"",
			"akka.remote.security.provider;version=\"[2.5,2.6)\"",
			"akka.remote.serialization;version=\"[2.5,2.6)\"",
			"akka.remote.transport;version=\"[2.5,2.6)\"",
			"akka.routing;version=\"[2.5,2.6)\"",
			"akka.stream;version=\"[2.5,2.6)\"",
			"akka.stream.actor;version=\"[2.5,2.6)\"",
			"akka.stream.extra;version=\"[2.5,2.6)\"",
			"akka.stream.javadsl;version=\"[2.5,2.6)\"",
			"akka.stream.impl;version=\"[2.5,2.6)\"",
			"akka.stream.impl.fusing;version=\"[2.5,2.6)\"",
			"akka.stream.impl.io;version=\"[2.5,2.6)\"",
			"akka.stream.impl.io.compression;version=\"[2.5,2.6)\"",
			"akka.stream.scaladsl;version=\"[2.5,2.6)\"",
			"akka.stream.stage;version=\"[2.5,2.6)\"",
			"akka.serialization;version=\"[2.5,2.6)\"",
			"akka.util;version=\"[2.5,2.6)\"",
			"com.typesafe.config;version=\"[0.4,2)\"",
			"com.typesafe.sslconfig.akka;version=\"[2.5,2.6)\"",
			"com.typesafe.sslconfig.akka.util;version=\"[2.5,2.6)\""
			)
		);

	def defaultImports = additionalImports (
		Seq (
			"com.github.nscala_time;resolution:=optional;version=\"[2.14,3)\"",
			"javax.management",
			"org.osgi.framework;version=\"[1.8,2)\"",
			"org.osgi.service.blueprint;version=\"[1,1.1)\"",
			"org.osgi.service.blueprint.container;version=\"[1,1.1)\"",
			"org.osgi.service.blueprint.reflect;version=\"[1,1.1)\"",
			"org.osgi.service.cm;version=\"[1.3,2)\"",
			"org.osgi.service.log;version=\"[1.3,2)\"",
			"org.slf4j;version=\"1.7.13\"",
			"scala;version=\"[2.12.4,2.13)\"",
			"scala.concurrent.stm;version=\"[0.8,1)\"",
			"scala.compat.java8;version=\"[0.8,1)\"",
			"scala.xml;version=\"[1.0,2)\"",
			"scala.xml.*;version=\"[1.0,2)\"",
			"scala.util.parsing.combinator;version=\"[1.0,2)\"",
			"scala.util.parsing.combinator.*;version=\"[1.0,2)\"",
			"scala.util.parsing.input;version=\"[1.0,2)\"",
			"scala.util.parsing.json;version=\"[1.0,2)\"",
			"scala.*;version=\"[2.12.4,2.13)\"",
			"shapeless;version=\"[2.3,3)\"",
			"*"
			)
		);


	def activator (name : String) : Seq[Setting[_]] =
		Seq (OsgiKeys.bundleActivator := Some (name));


	def additionalHeaders (entries : Seq[(String, String)]) : Seq[Setting[_]] =
		Seq (OsgiKeys.additionalHeaders ++= entries.toMap);


	def additionalImports (packages : Seq[String]) : Seq[Setting[_]] =
		OsgiKeys.importPackage ++= {
			val existing = OsgiKeys.importPackage.value;

			packages filterNot (p => existing.contains (p));
			}


	def bundleVersion () : Seq[Setting[_]] =
		OsgiKeys.bundleVersion ~= {
			ver =>

			ver stripSuffix ("-SNAPSHOT");
			}


	def bundleVersion (explicit : String) : Seq[Setting[_]] =
		OsgiKeys.bundleVersion := explicit;


	def dominoActivator () : Seq[Setting[_]] =
		activator ("${classes;CONCRETE;EXTENDS;domino.DominoActivator}");


	/// For Declarative Services support
	def components (resources : String *) : Seq[Setting[_]] =
		Seq (
			OsgiKeys.additionalHeaders ++= Map (
				"Service-Component" -> (
					resources.toSeq.map ("OSGI-INF/" + _).mkString (",", ",", "")
					)
				)
			);


	def embed (project : Project) : Setting[_] =
		OsgiKeys.embeddedJars += ((packageBin in Compile) in project).value;


	def embed (jars : Seq[ModuleID]) : Setting[_] =
		OsgiKeys.embeddedJars ++=
			(dependencyClasspath in Compile).value.files.filter {
				file =>

				jars.exists {
					mid =>

					file.getPath.contains (
						"/" + mid.organization + "/" + mid.name
						);
					}
			}


	def explode (project : Project) : Setting[_] =
		OsgiKeys.explodedJars += ((packageBin in Compile) in project).value;


	def explode (jars : Seq[ModuleID]) : Setting[_] =
		OsgiKeys.explodedJars ++=
			(dependencyClasspath in Compile).value.files.filter {
				file =>

				jars.exists {
					mid =>

					file.getPath.contains (
						"/" + mid.organization + "/" + mid.name
						);
					}
			}


	def exports (base : String)
		(packages : Seq[String] = Seq (".*"))
		: Seq[Setting[_]] =
		supportsOsgi () ++
			Seq (
				OsgiKeys.exportPackage := Seq ("!" + base + ".internal.*") ++
					packages.map {
						pkg =>

						if (pkg.startsWith ("."))
							base + pkg;
						else
							pkg;
						},

				OsgiKeys.privatePackage := Seq (base + ".internal.*")
				) ++
			bundleVersion ();


	def explicitExports (exportVersion : String)
		(packages : Seq[String])
		: Seq[Setting[_]] =
		Seq (
			OsgiKeys.exportPackage := packages map {
				pkg =>

				"%s;version=\"%s\"".format (pkg, exportVersion);
				},

			OsgiKeys.importPackage := packages map {
				pkg =>

				"%s;version=\"%s\"".format (pkg, exportVersion);
				}
			);


	def fragment (host : String) : Seq[Setting[_]] =
	{
		val withVersion =
			if (host.startsWith ("com.github.osxhacker.foundation"))
				"%s;bundle-version=\"%s\"".format (
					host,
					BuildProperties ("version") stripSuffix ("-SNAPSHOT")
					);
			else
				host;

		additionalHeaders (("Fragment-Host" -> withVersion) :: Nil);
	}


	def imports (base : String, includeBase : Boolean = true)
		(packages : Seq[String] = Seq.empty) : Seq[Setting[_]] =
	{
		val declared = Seq (
			OsgiKeys.importPackage := {
				Seq (
					"!" + base + ".internal.*",
					"!aQute.bnd.annotation.*"
					) ++ packages;
				}
			);
		
		if (includeBase)
			declared ++ projectImports (Seq (base));
		else
			declared;
	}


	def projectExports (packages : Seq[String]) : Seq[Setting[_]] =
		OsgiKeys.exportPackage ++= {
			val exportProjectVersion = version.value replace ("-SNAPSHOT", "");

			packages map {
				pkg =>

				"%s;version=\"%s\"".format (pkg, exportProjectVersion);
				}
			}


	def prependImports (imports : Seq[Setting[_]]) : Seq[Setting[_]] =
		Seq (
			OsgiKeys.importPackage := {
				Seq ("!aQute.bnd.annotation.*")
				}
			) ++ imports;


	def projectImports (packages : Seq[String]) : Seq[Setting[_]] =
		OsgiKeys.importPackage ++= {
			val existing = OsgiKeys.importPackage.value;
			val importProjectVersion = version.value replace ("-SNAPSHOT", "");

			packages filterNot (p => existing.contains (p)) map {
				pkg =>

				if (!pkg.contains (";version="))
					"%s;version=\"%s\"".format (pkg, importProjectVersion);
				else
					pkg;
				}
			}


	def projectImports (base : String)
		(packages : Seq[String])
		: Seq[Setting[_]] =
		projectImports (packages map (base + _));


	def resourcesFragment (host : String)
		: Seq[Setting[_]] =
		supportsOsgi () ++
		bundleVersion () ++
		fragment (host);


	def simple (base : String, topLevel : String) : Seq[Setting[_]] =
		simple (base, topLevel, Nil);


	def simple (base : String, topLevel : String, otherProjects : Seq[String])
		: Seq[Setting[_]] =
		exports (base + topLevel) () ++
			imports (base + topLevel) () ++
			projectImports (base) (otherProjects) ++
			akkaImports ++
			defaultImports;


	def supportsOsgi () : Seq[Setting[_]] = osgiSettings;
}

