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

package com.github.osxhacker.foundation.models.core.scenario

import java.io.InputStream

import scala.concurrent.ExecutionContext

import akka.NotUsed
import akka.stream.scaladsl._
import akka.util.{
	ByteString,
	ByteStringBuilder
	}

import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.Scenario
import com.github.osxhacker.foundation.models.core.akkax
import com.github.osxhacker.foundation.models.core.functional
import com.github.osxhacker.foundation.models.core.functional.futures.FutureEither
import com.github.osxhacker.foundation.models.core.net.URL


/**
 * The '''ReadURLContent''' type defines the Domain Object Model
 * [[com.github.osxhacker.foundation.models.core.Scenario]] responsible for resolving the
 * contents of an arbitrary [[com.github.osxhacker.foundation.models.core.net.URL]].  The
 * resultant [[akka.stream.scaladsl.Flow]] can be used an arbitrary number of
 * times and, by definition, is referentially transparent.
 * 
 * @author osxhacker
 *
 */
final case class ReadURLContent ()
	extends Scenario[Flow[URL, ByteString, NotUsed]]
{
	/// Class Imports
	import akkax.streams._


	/// Instance Properties
	private val steps : Flow[URL, ByteString, NotUsed] =
		Flow[URL].map (_.openStream ())
			.map (load);


	override def apply () : Flow[URL, ByteString, NotUsed] = steps;


	private def load (stream : InputStream) : ByteString =
	{
		try
		{
			val buffer = new Array[Byte] (4096);
			val builder = new ByteStringBuilder ();
			var read = 0;
			
			do
			{
				read = stream.read (buffer);

				if (read > 0)
					builder.putBytes (buffer, 0, read);
			} while (read > 0);
			
			return builder.result ();
		}
		
		finally
		{
			stream.close ();
		}
	}
}

