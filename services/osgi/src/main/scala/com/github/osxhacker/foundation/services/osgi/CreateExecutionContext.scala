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

import java.util.concurrent.ExecutorService

import scala.concurrent.ExecutionContext


/**
 * The '''CreateExecutionContext''' type provides the ability to create
 * [[scala.concurrent.ExecutionContext]] instances within an OSGi Blueprint
 * configuration.
 *
 * @author osxhacker
 */
final class CreateExecutionContext (
	private val executor : ExecutorService
	)
{
	/**
	 * The fromExecutorService method is a simple decorator for the
	 * [[scala.concurrent.ExecutionContext]] `fromExecutorService` method
	 * and exists only due to OSGi Blueprint limitations disallowing references
	 * to Scala `object`s.
	 */
	def fromExecutorService : ExecutionContext =
		ExecutionContext.fromExecutorService (executor);
}

