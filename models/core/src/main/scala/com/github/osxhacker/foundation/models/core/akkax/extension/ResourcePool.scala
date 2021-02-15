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

package com.github.osxhacker.foundation.models.core.akkax.extension

import scala.concurrent.duration.{
	Deadline,
	Duration
	}

import akka.actor.Extension
import org.apache.commons.pool2._
import org.apache.commons.pool2.impl.{
	DefaultPooledObject,
	GenericObjectPool,
	GenericObjectPoolConfig
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.error.{
	ApplicationError,
	InitializationError,
	ResourceUsageError,
	TimeoutError
	}

import com.github.osxhacker.foundation.models.core.functional

/**
 * The '''ResourcePool''' type defines the common behaviour required of a
 * pooled resource manager made available as an [[akka.actor.Extension]].
 * Aside from resource creaetion, which is delegated to the implementation of
 * the `factory` method, concrete types need only specify the ''ResourceT''.
 *
 * @author osxhacker
 *
 */
abstract class ResourcePool[ResourceT <: AnyRef] (
	val min : Int,
	val max : Int
	)
	extends Extension
{
	/// Class Imports
	import \/.fromTryCatchThrowable
	import functional.kestrel._
	import syntax.bind._


	/// Class Types
	/**
	 * The '''ResourceFactory''' type must be completed by the concrete pool
	 * implementation by providing a `create () : ResourceT` method.  Also,
	 * the concrete pools are insulated from the Apache Commons Pool2 API.
	 *
	 * @author osxhacker
	 *
	 */
	abstract class ResourceFactory ()
		extends BasePooledObjectFactory[ResourceT]
	{
		final override def activateObject (
			pooledObject : PooledObject[ResourceT]
			)
			: Unit =
			pooledObject.getObject.synchronized {
				activate (pooledObject.getObject);
				}


		final override def destroyObject (
			pooledObject : PooledObject[ResourceT]
			)
			: Unit =
			destroy (pooledObject.getObject);


		final override def passivateObject (
			pooledObject : PooledObject[ResourceT]
			)
			: Unit =
			passivate (pooledObject.getObject);


		final override def wrap (resource : ResourceT)
			: PooledObject[ResourceT] =
			new DefaultPooledObject[ResourceT] (resource);


		def activate (resource : ResourceT) : Unit =
		{
		}


		def destroy (resource : ResourceT) : Unit =
		{
		}


		def passivate (resource : ResourceT) : Unit =
		{
		}
	}


	/// Instance Properties
	/**
	 * The factory property must be provided by concrete
	 * '''ResourceFactory'''s.
	 */
	protected def factory : ResourceFactory;

	private lazy val pool = {
		val config = new GenericObjectPoolConfig[ResourceT] ();

		config.setMaxTotal (max);
		config.setMinIdle (min);

		new GenericObjectPool[ResourceT] (factory, config);
		}


	/**
	 * The apply method is a higher-order functor which attempts to `borrow	`
	 * a ''ResourceT'' and invoke '''f''' with it.  Should '''f''' throw an
	 * exception, the ''ResourceT'' is invalidated before producing the
	 * error.
	 */
	def apply[A] (f : ResourceT => A)
		(implicit deadline : Deadline)
		: ApplicationError \/ A =
		borrow () >>= use (f);


	private def borrow ()
		(implicit deadline : Deadline)
		: ApplicationError \/ ResourceT =
		fromTryCatchThrowable[ResourceT, Throwable] {
			pool.borrowObject (deadline.timeLeft.toMillis);
			}
			.leftMap[ApplicationError] (e =>
				InitializationError ("unable to borrow resource", Some (e))
				);


	private def errorUsingResource (resource : ResourceT)
		(error : Throwable)
		: ApplicationError =
	{
		pool.invalidateObject (resource);

		return ResourceUsageError (error);
	}


	private def use[A] (f : ResourceT => A)
		: ResourceT => ApplicationError \/ A =
		resource => fromTryCatchThrowable[A, Throwable] (f (resource))
			.kestrel (_ => pool.returnObject (resource))
			.leftMap (errorUsingResource (resource) _);
}

