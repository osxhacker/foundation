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

package com.github.osxhacker.foundation.services.osgi.scenario

import scala.language.{
	higherKinds,
	postfixOps
	}

import org.osgi.framework.{
	Bundle,
	BundleContext
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	Scenario,
	Specification
	}

/**
 * The '''ScanBundles''' type defines an OSGi-based
 * [[com.github.osxhacker.foundation.models.core.Scenario]] which identifies
 * [[org.osgi.framework.Bundle]]s which satisfy the given `specification`.
 * 
 * @author osxhacker
 *
 */
final case class ScanBundles[M[+_]] (
	private val specification : Specification[Bundle]
	)
	(implicit a : Applicative[M], m : Monoid[M[Bundle]])
	extends Scenario[BundleContext => M[Bundle]]
{
	/// Class Imports
	import syntax.all._
	import syntax.std.boolean._


	override def apply () : BundleContext => M[Bundle] =
		context => {
			val bundles = context.getBundles ();
			
			bundles.foldLeft (mzero[M[Bundle]]) {
				(accum, bundle) =>

				accum |+| (specification (bundle) ?? bundle.point[M]);
				}
			}
}
