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

package com.github.osxhacker.foundation.models.core

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}


import error.{
	ApplicationError,
	DomainValueError
	}


/**
 * The '''net''' `package` defines types responsible for modelling network
 * abstractions.  Many are defined by
 * [[https://tools.ietf.org/html/ IETF Standards Documents]] and, when
 * possible, links to relevant IETF RFC's are provided.
 *
 * @author osxhacker
 *
 */
package object net
{
	/// Class Types
	type InetAddress = java.net.InetAddress
	type InetSocketAddress = java.net.InetSocketAddress
	type URI = java.net.URI
	type URISyntaxException = java.net.URISyntaxException
	type URL = java.net.URL
	
	
	object all
		extends ToEndpointParameterOps
			with RichURIImplicits
			with ToRichURIOps
		
		
	object endpoint
		extends ToEndpointParameterOps


	object uri
		extends ToEndpointParameterOps
			with RichURIImplicits
			with ToRichURIOps


	object InetAddress
		extends StringValidations
	{
		/// Class Imports
		import \/.fromTryCatchThrowable
		import Scalaz._


		/// Instance Properties
		private val BadAddress : Throwable => ApplicationError =
			e => DomainValueError ("Invalid IP", e);
			
		private val MaxLength = 16;
		
		
		/**
		 * The apply method is provided to lift `getByName` into a functional
		 * result by trapping exceptions
		 */
		def apply (candidate : String) : ApplicationError \/ InetAddress =
			possiblyValid (candidate) >>= (tryParsing _);
				
				
		private def possiblyValid (candidate : String)
			: ApplicationError \/ String =
			fromString[String] {
				implicit v =>
					
				(notEmpty >==> withinMaxLength (MaxLength)).run (candidate);
				}
			
			
		private def tryParsing (candidate : String)
			: ApplicationError \/ InetAddress =
			BadAddress <-: fromTryCatchThrowable[InetAddress, Throwable] {
				java.net.InetAddress.getByName (candidate);
				}
	}
}

