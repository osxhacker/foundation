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

import akka.actor.FSM

import error.ActorTerminatedError


/**
 * The '''ErrorPolicyAware''' trait defines support for a declarative error
 * handling policy for use in [[akka.actor.FSM]]-based [[akka.actor.Actor]]s.
 *
 * @author osxhacker
 *
 */
trait ErrorPolicyAware
{
	/// Self Type Constraints
	this : FSM[_, _] =>


	/**
	 * The onError method should be whenever an unrecoverable problem has been
	 * encountered in the [[akka.actor.FSM]].  The default implementation will
	 * unconditionally `throw`an
	 * [[com.github.osxhacker.foundation.models.core.akkax.error.ActorTerminatedError]].
	 */
	def onError (cause : Throwable) : State =
		throw ActorTerminatedError (self, Some (cause));
}


/**
 * The '''StopOnErrorPolicy''' trait provides an
 * [[com.github.osxhacker.foundation.models.core.akkax.ErrorPolicyAware]] implementation
 * which `stop`s the [[akka.actor.FSM]] instead of `throw`ing an
 * [[com.github.osxhacker.foundation.models.core.error.ApplicationError]].
 */
trait StopOnErrorPolicy
{
	/// Self Type Constraints
	this : FSM[_, _]
		with ErrorPolicyAware
		=>


	abstract override def onError (cause : Throwable) : State =
		stop (FSM.Failure (cause));
}

