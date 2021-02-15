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

package com.github.osxhacker.foundation.models

import scalaz.{
	Failure => _,
	Success => _,
	_
	}


/**
 * =Overview=
 * 
 * The '''core''' `package` defines types related to the domain model used
 * in the system.  There are two main implementation techniques a developer
 * needs to be aware of when using the types defined here.
 * 
 * ==Value Objects==
 * 
 * A
 * [[http://stochastyk.blogspot.com/2008/05/value-objects-in-domain-driven-design.html Value Object]]
 * is, as the referenced article describes and originally defined in the
 * [[http://www.amazon.com/exec/obidos/ASIN/0321125215/domainlanguag-20 Evans book]],
 * a type which has no intrinsic identity beyond that which its properties
 * provide.
 * 
 * In the `com.github.osxhacker.foundation.models.core` code base, these types are
 * reified in the form of `case class`es and are ''immutable'' once created.
 * 
 * ==Entity Types==
 * 
 * Abstractions which have an identity not strictly based on their properties
 * are embodied in ''entity'' types.  To insulate persistent storage concerns
 * from other components, entities are defined as `trait`s and implemented
 * in a manner most applicable/optimal for the storage technology involved.
 * 
 * Something to note when familiarizing oneself with entity types are a lack of
 * `public` ''property accessors''.  This is by design, as entity attributes
 * are an implementation detail and ''not'' to be depended upon by types
 * collaborating with them.
 * 
 * ==Aggregate Root Types==
 * 
 * As with Entity Types above, Aggregate Root types have identity which
 * transcend their properties.  What differentiates the two type categories
 * is the fact that Aggregate Roots is defined in terms of a ''root entity''
 * as well as other entities and value objects, so long as any acceptable
 * modification to an Aggregate Root results in ''all'' of its constituent
 * invariants being completely consistent within a single transaction.
 * 
 * This requirement has implications on the use of Aggregate Root types and,
 * as such, it is expected that there may be Aggregate Roots which appear
 * very similar.  When these situations exist, they are only allowed to
 * remain when a documented reason justifies the context.
 * 
 * @author osxhacker
 *
 */
package object core
{
	/// Class Types
	type URI = java.net.URI
	type URISyntaxException = java.net.URISyntaxException
}

