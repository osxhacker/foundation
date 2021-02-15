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

import scalaz.{
	Failure => _,
	Name => _,
	Success => _,
	_
	}


/**
 * The '''FilterSpecificationSpec''' type defines the unit tests for the
 * [[com.github.osxhacker.foundation.services.osgi.FilterSpecification]] type to
 * ensure fitness for purpose as well as to serve as an exemplar for use.
 *
 * @author osxhacker
 */
final class FilterSpecificationSpec
	extends ProjectSpec
{
	/// Class Imports
	import filter._


	/// Class Types
	sealed trait SomeInterface
	

	sealed trait SampleService
		extends Serializable
			with SomeInterface


	/// Instance Properties
	val someInterfaceTypeName = classOf[SomeInterface].getName;
	
	
	"A FilterSpecification" must support {
		"filtering by Blueprint name" in {
			val blueprintName = "blueprint-bean-name";
			val spec = ComponentName[SampleService] (blueprintName);

			assert (spec.interface.isEmpty);
			assert (spec.name.isDefined);
			assert (spec.property.isEmpty);
			assert (
				spec.toFilter ===
				s"(osgi.service.blueprint.compname=${blueprintName})"
				);
			}

		"filtering by interface type" in {
			val spec = ImplementsInterface[SampleService, Serializable] ();

			assert (spec.name.isEmpty);
			assert (spec.interface.isDefined);
			assert (spec.property.isEmpty);
			assert (spec.toFilter === "(objectClass=scala.Serializable)");
			}

		"arbitrary property filters" in {
			val spec = ServiceProperty[SampleService] ("some-property" -> 99);
			
			assert (spec.name.isEmpty);
			assert (spec.interface.isEmpty);
			assert (spec.property.isDefined);
			assert (spec.toFilter === "(some-property=99)");
			}

		"decomposing conjunctions" in {
			val blueprintName = "blueprint-bean-name";
			val spec = ComponentName[SampleService] (blueprintName) &&
				ImplementsInterface[SampleService, Serializable] ();

			assert (
				spec.toFilter ===
				s"""(
				|&
				|(osgi.service.blueprint.compname=${blueprintName})
				|(objectClass=scala.Serializable)
				|)
				|""".stripMargin.replaceAllLiterally ("\n", "")
				);
			}

		"decomposing disjunctions" in {
			val blueprintName = "blueprint-bean-name";
			val spec = ComponentName[SampleService] (blueprintName) ||
				ImplementsInterface[SampleService, Serializable] ();

			assert (
				spec.toFilter ===
				s"""(
				||
				|(osgi.service.blueprint.compname=${blueprintName})
				|(objectClass=scala.Serializable)
				|)
				|""".stripMargin.replaceAllLiterally ("\n", "")
				);
			}

		"decomposing negations" in {
			val blueprintName = "blueprint-bean-name";
			val spec = !ComponentName[SampleService] (blueprintName);

			assert (
				spec.toFilter ===
				s"""(
				|!
				|(osgi.service.blueprint.compname=${blueprintName})
				|)
				|""".stripMargin.replaceAllLiterally ("\n", "")
				);
			}
		
		"complex decomposition" in {
			val blueprintName = "blueprint-bean-name";
			val spec = !ComponentName[SampleService] (blueprintName) && (
				ImplementsInterface[SampleService, SomeInterface] () ||
				ServiceProperty[SampleService] ("selector" -> "yes")
				);
			
			assert (
				spec.toFilter ===
				s"""(
				|&
				|(!(osgi.service.blueprint.compname=${blueprintName}))
				|(|
				|(objectClass=${someInterfaceTypeName})
				|(selector=yes)
				|)
				|)
				|""".stripMargin.replaceAllLiterally ("\n", "")
				);
			}
		}

	it must {
		"have a value-based toString implementation" in {
			val blueprintName = "blueprint-bean-name";
			val spec = !ComponentName[SampleService] (blueprintName) && (
				ImplementsInterface[SampleService, SomeInterface] () ||
				ServiceProperty[SampleService] ("selector" -> "yes")
				);
			
			assert (spec.toString.contains (blueprintName));
			assert (spec.toString.contains ("selector"));
			assert (spec.toString.contains ("&&"));
			assert (spec.toString.contains ("||"));
			assert (spec.toString.contains ("!"));
			}
		}
}
