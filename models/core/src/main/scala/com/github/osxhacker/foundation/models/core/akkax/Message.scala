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


/**
 * The '''Message''' type serves as the root `trait` for ''all'' messages
 * exchanged between system `Actor`s and their clients.
 *
 * @author osxhacker
 *
 */
trait Message[A <: Message[A]]
	extends Serializable
	
	
/**
 * All messages originally sent to an Akka `Actor` *should* be derived from
 * this '''Request''' type.  Doing so establishes the expected
 * [[com.github.osxhacker.foundation.models.core.akkax.Response]] type and helps regain
 * type safety as quickly as possible.
 *
 * @author osxhacker
 *
 */
trait Request[A <: Response[A]]
	extends Message[Request[A]]


/**
 * The '''Response''' type reifies the message category sent to issuers of a
 * [[com.github.osxhacker.foundation.models.core.akkax.Request]] from a system-defined
 * `Actor`.
 *
 * @author osxhacker
 *
 */
trait Response[A <: Response[A]]
	extends Message[Response[A]]

