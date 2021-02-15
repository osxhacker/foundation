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
package extension

import akka.actor._


/**
 * The '''LogUnhandledMessages''' `trait` is satisfies the
 * [[akka.actor.Extension]] contract and exists solely for this purpose.
 *
 * @author osxhacker
 */
sealed class LogUnhandledMessages
	extends Extension
	
	
object LogUnhandledMessages
	extends ExtensionId[LogUnhandledMessages]
		with ExtensionIdProvider
{
	/// Class Types
	/**
	 * The '''UnhandledMessages''' type defines an [[akka.actor.Actor]] which
	 * logs *any* unknown message sent to an arbitrary [[akka.actor.Actor]] within
	 * the configured [[akka.actor.ActorSystem]].
	 *
	 * @author osxhacker
	 */
	final class UnhandledMessages
		extends ActorStack
			with Slf4jLogging
	{
		override def preStart () : Unit =
		{
			super.preStart ();
			
			context.system.eventStream.subscribe (self, classOf[UnhandledMessage]);
		}
		
		
		override def receive =
		{
			case UnhandledMessage (message, from, recipient) =>
				error (
					"Unhandled message detected. from=%s to=%s message=%s",
					from,
					recipient,
					message
					);
		}
	}


	override def createExtension (system : ExtendedActorSystem)
		: LogUnhandledMessages =
	{
		system.actorOf (Props[UnhandledMessages]);
		
		return new LogUnhandledMessages ();
	}
	
	
	override def lookup () = this;
}

