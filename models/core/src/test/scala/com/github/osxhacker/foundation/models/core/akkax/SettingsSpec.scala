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

import scala.concurrent.duration._
import scala.language.postfixOps

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}

import com.typesafe.config.{
	Config,
	ConfigFactory
	}

import com.github.osxhacker.foundation.models.core.{
	DeploymentEnvironment,
	ProjectSpec
	}


/**
 * The '''SettingsSpec''' type defines unit tests for the
 * [[com.github.osxhacker.foundation.models.core.akkax.Settings]] Domain Object
 * Model type which ensure expected behaviour.
 *
 * @author osxhacker
 */
final class SettingsSpec
	extends ProjectSpec
{
	/// Class Imports
	import syntax.id._


	/// Class Types
	case class SampleSettings (override val config : Config)
		extends Settings ("test")
	{
		lazy val boolean = key ("boolean") |> asBoolean;
		lazy val duration = key ("duration") |> asDuration;
		lazy val int = key ("int") |> asInt;
		lazy val string = key ("string") |> asString;
	}


	/// Instance Properties
	val testConfig = ConfigFactory.parseString (
		"""
		|foundation {
		|	deployment = "urn:deployment:unit-test"
		|	test {
		|		boolean = true
		|		string = "hello, world!"
		|		duration = 1 minute
		|		int = 12345
		|	}
		|}
		|
		""".stripMargin
		);
	
	
	"The Settings type" must {
		val settings = SampleSettings (testConfig);

		"resolve the deployment environment" in {
			assert (settings.deployment.isRight);
			}

		"resolve Booleans from a Config" in {
			assert (settings.boolean === true);
			}

		"resolve Durations from a Config" in {
			assert (settings.duration === (1 minute));
			}

		"resolve Ints from a Config" in {
			assert (settings.int === 12345);
			}

		"resolve Strings from a Config" in {
			assert (!settings.string.isEmpty, "Able to resolve a value");
			assert (settings.string === "hello, world!");
			}
		}
}

