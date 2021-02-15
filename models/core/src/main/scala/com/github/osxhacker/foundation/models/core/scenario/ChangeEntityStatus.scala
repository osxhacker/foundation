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

import scalaz.{
	Failure => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.{
	DiscreteStatus,
	Scenario
	}

import com.github.osxhacker.foundation.models.core.entity.{
	Entity,
	EntityStatus
	}


/**
 * The '''ChangeEntityStatus''' type defines the
 * [[com.github.osxhacker.foundation.models.core.Scenario]] for altering an arbitrary
 * [[com.github.osxhacker.foundation.models.core.Entity]]'s status.  If the given
 * '''entity''' is already in the '''desired'''
 * [[com.github.osxhacker.foundation.models.core.DiscreteStatus]], then the
 * '''entity''' is produced unchanged.
 *
 * @author osxhacker
 */
final case class ChangeEntityStatus[
	E <: Entity[E] with EntityStatus[E, DS],
	DS <: DiscreteStatus[DS]
	] (
		private val entity : E,
		private val desired : DS
		)
	extends Scenario[E#ValidatedChange[E]]
{
	/// Class Imports
	import State._
	import syntax.either._
	import syntax.state._
	

	override def apply () =
		entity.unlessStatusOf[E#ValidatedChange] (desired) {
			for {
				current <- init[entity.EntityType]
				prior <- entity.changeStatus (desired)
				updated <- get[entity.EntityType]
				} yield updated;
			} ||| (entity.right);
}

