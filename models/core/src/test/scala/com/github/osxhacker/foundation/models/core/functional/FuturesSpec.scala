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
package functional

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

import scalaz._


/**
 * The '''FuturesSpec''' type defines the unit tests which verify monadic
 * support for [[scala.concurrent.Future]]s.
 *
 * @author osxhacker
 */
class FuturesSpec
	extends ProjectSpec
{
	/// Class Imports
	import futures._
	import syntax.comonad._
	import syntax.monad._
	
	
	/// Instance Properties
	implicit val ec : ExecutionContext = ExecutionContext.Implicits.global;
	
	private val obey = afterWord ("obey");


	implicit def futureEqual[T] : Equal[Future[T]] =
		new Equal[Future[T]] {
			override def equal (a1 : Future[T], a2 : Future[T]) =
				Await.result (for {
					a <- a1
					b <- a2
					} yield a == b,
					5 seconds
				)
			}
	

	"A Future" when {
		"used as a Monad" must obey {
			import futures.monad._
			
			val fm = implicitly[Monad[Future]];
				
			"associative bind" in {
				assert (
					fm.monadLaw.associativeBind (
						Future.successful('c'),
						Future.successful[Char],
						Future.successful[Char]
						)
					);
				}
			
			"left identity" in {
				assert (fm.monadLaw.leftIdentity (1, Future.successful[Int]));
				}
			
			"right identity" in {
				assert (fm.monadLaw.rightIdentity (Future.successful ("x")));
				}
			}
		
		"used as a Monoid" must obey {
			import std.anyVal._
			
			implicit val fm = implicitly[Monoid[Future[Int]]];
			
			"associative" in {
				assert (
					fm.monoidLaw.associative (
						Future.successful (1),
						Future.successful (2),
						Future.successful (3)
						)
					);
				}
			
			"left identity" in {
				assert (fm.monoidLaw.leftIdentity (Future.successful[Int] (99)));
				}
			
			"right identity" in {
				assert (fm.monoidLaw.rightIdentity (Future.successful[Int] (99)));
				}
			}
		
		"used as a Comonad" should {
			"provide a Comonad implicitly" in {
				assertCompiles ("""
					import futures.comonad._
				
					implicit val deadline = 5 seconds fromNow;
					val cm = implicitly[Comonad[Future]];
					""");
				}

			"be usable with an implicit Deadline" in {
				import futures.comonad._
				
				implicit val deadline = 5 seconds fromNow;
				val content = "extracted from a Future";
				
				assert (Future (content).copoint === content);
				}
			}
		
		"used as a Bifunctor" must {
			import futures.bifunctor._
			import futures.comonad._
			import std.tuple._
			import syntax.bifunctor._
			
			implicit val deadline = 5 seconds fromNow;

			"support bimap" in {
				val tuple = (1, "one");
				val future = tuple.point[Future];
				val mapped = future bimap (_ + 1, _.reverse);
				val result = mapped copoint;
				val expected = (2, "eno");
				
				assert (result === expected);
				}
			}
		}
}
