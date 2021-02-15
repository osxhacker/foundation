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

package com.github.osxhacker.foundation.models.security

import akka.actor.ActorSystem
import com.typesafe.config.Config
import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Identifier
import com.github.osxhacker.foundation.models.core.akkax.{
	DefaultDurationSettingsAware,
	Settings
	}

import com.github.osxhacker.foundation.models.core.net.URN


/**
 * The '''SecuritySettings''' type defines a Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.akkax.Settings]] type which
 * provides access to run-time configuration settings for the
 * `com.github.osxhacker.foundation.models.security` package.
 * 
 * ==Knobs==
 * 
 *   - '''foundation.security.error-timeout''': How long to wait while in an
 *   `Errored` state before stopping an [[akka.actor.Actor]].
 * 
 *   - '''foundation.security.loading-timeout''': How long to wait for an
 *   [[akka.actor.Actor]] to resolve its collaborators.
 *
 *   - '''foundation.security.retries''': Number of times to retry operations
 *   which fail.
 * 
 *   - '''foundation.security.time-range''':
 *   [[scala.concurrent.duration.Duration]] for the maximum retries.
 * 
 *   - '''foundation.security.vault-copies''': Optional number of duplicate
 *   [[com.github.osxhacker.foundation.models.security.vault.Vault]]s to cycle
 *   through (default is `5`).
 *
 * @author osxhacker
 *
 */
final case class SecuritySettings (private val system : ActorSystem)
	extends Settings ("security")
		with DefaultDurationSettingsAware
{
	/// Class Imports
	import syntax.id._


	/// Instance Properties
	override val config : Config = system.settings.config;

	lazy val errorTimeout = durationOrDefault ("error-timeout");
	lazy val loadingTimeout = durationOrDefault("loading-timeout");
	lazy val passwords = key ("passwords") |> asURI;
	lazy val retries = key ("retries") |> asInt;
	lazy val timeRange = key ("time-range") |> asDuration;
	lazy val vaultCopies = key ("vault-copies") |> asMaybeInt;
}
