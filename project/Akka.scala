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


/**
 * The '''Akka''' `object` defines build settings related to
 * [[http://akka.io/docs/ Akka]].
 */
object Akka
{
	/// Instance Properties
	val version = "2.5.31";
	val httpVersion = "10.1.11";


	/// Compile Dependencies
	lazy val unused = Seq (
		"com.typesafe.akka" %% "akka-camel" % version,
		"com.typesafe.akka" %% "akka-multi-node-testkit" % version
		);

	lazy val compile = Seq (
		"com.typesafe" % "config" % "1.3.4",
		"com.typesafe" %% "ssl-config-core" % "0.3.8" excludeAll (
			ExclusionRule ("com.typesafe", "config"),
			ExclusionRule (
				"org.scala-lang.modules",
				"scala-parser-combinators_2.12"
				)
			),

		"com.typesafe.akka" %% "akka-actor" % version,
		"com.typesafe.akka" %% "akka-agent" % version,
		"com.typesafe.akka" %% "akka-cluster" % version,
		"com.typesafe.akka" %% "akka-cluster-metrics" % version,
		"com.typesafe.akka" %% "akka-cluster-sharding" % version,
		"com.typesafe.akka" %% "akka-cluster-tools" % version,
		"com.typesafe.akka" %% "akka-osgi" % version,
		"com.typesafe.akka" %% "akka-persistence" % version,
		"com.typesafe.akka" %% "akka-persistence-query" % version,
		"com.typesafe.akka" %% "akka-remote" % version,
		"com.typesafe.akka" %% "akka-slf4j" % version,
		"com.typesafe.akka" %% "akka-stream" % version
		);

	lazy val http = Seq (
		"com.typesafe.akka" %% "akka-http-core" % httpVersion,
		"com.typesafe.akka" %% "akka-http" % httpVersion,
		"com.typesafe.akka" %% "akka-http-jackson" % httpVersion,
		"com.typesafe.akka" %% "akka-http-spray-json" % httpVersion,
		"com.typesafe.akka" %% "akka-http-testkit" % httpVersion % "test",
		"com.typesafe.akka" %% "akka-http-xml" % httpVersion,
		"com.typesafe.akka" %% "akka-parsing" % httpVersion
		);

	/// Test Dependencies
	lazy val test = Seq (
		"com.typesafe.akka" %% "akka-persistence-tck" % version % "test",
		"com.typesafe.akka" %% "akka-stream-testkit" % version % "test",
		"com.typesafe.akka" %% "akka-testkit" % version % "test"
		);
}

