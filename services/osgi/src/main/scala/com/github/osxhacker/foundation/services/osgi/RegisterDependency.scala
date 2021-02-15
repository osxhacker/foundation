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

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

import akka.actor.ActorSystem
import domino.DominoActivator
import domino.capsule.Capsule
import domino.logging.Logging
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.akkax.extension._


/**
 * The '''RegisterDependencyCapsule''' type defines a
 * [[domino.capsule.Capsule]] which will `register` an arbitrary `AnyRef`
 * which has defined for it a
 * [[com.github.osxhacker.foundation.models.core.akkax.extension.DependencyLocator]].
 *
 * @author osxhacker
 *
 */
final class RegisterDependencyCapsule[
	A <: AnyRef,
	L <: DependencyLocator[A],
	] (
		private val activator : DominoActivator with Logging,
		private val provider : DependencyProvider[A , L]
		)
		(f : => A)
		(
			implicit val system : ActorSystem,
			ct : ClassTag[A],
			tt : TypeTag[A]
		)
	extends Capsule
{
	/// Class imports
	import activator._


	/// Instance Properties 
	private lazy val instance : A = f;


	override def start () : Unit =
	{
		log.debug ("Providing " + instance.getClass.getName);
		provider.register (instance);
		instance.providesService[A];
	}


	override def stop () : Unit =
	{
	}
}


/**
 * The '''RegisterDependency''' type provides syntactic convenience for adding
 * a [[com.github.osxhacker.foundation.services.osgi.RegisterDependencyCapsule]] to the
 * `activator`.  Typical use resembles:
 * 
 * {{{
 * val registrar = new RegisterDependency (this);
 * 
 * def someMethod ()
 * {
 * 	import registrar._
 * 
 * 	registerDependency (SomeDependencyLocator) (new Dependency);
 * }
 * }}}
 *
 * @author osxhacker
 */
class RegisterDependency (private val activator : DominoActivator with Logging)
{
	/**
	 * The registerDependency method provides syntactic convenience for adding
	 * a [[com.github.osxhacker.foundation.services.osgi.RegisterDependencyCapsule]] to the
	 * `activator`.  Typical use resembles:
	 * 
	 * {{{
	 * val registrar = new RegisterDependency (this);
	 * 
	 * def someMethod ()
	 * {
	 * 	import registrar._
	 * 
	 * 	registerDependency (SomeDependencyLocator) (new Dependency);
	 * }
	 * }}}
	 */
	def registerDependency[
		A <: AnyRef,
		L <: DependencyLocator[A],
		] (provider : DependencyProvider[A , L])
		(f : => A)
		(
			implicit system : ActorSystem,
			ct : ClassTag[A],
			tt : TypeTag[A]
		)
		: Unit =
	{
		val capsule = new RegisterDependencyCapsule[A, L] (activator, provider) (f);
		
		activator.addCapsule (capsule);
	}
}
