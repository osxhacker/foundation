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

package com.github.osxhacker.foundation.models.core.text

import scala.language.postfixOps

import org.scalatest.DiagrammedAssertions
import scalaz.{
	Failure => _,
	Sink => _,
	Source => _,
	Success => _,
	_
	}

import com.github.osxhacker.foundation.models.core.ProjectSpec
import com.github.osxhacker.foundation.models.core.error.ApplicationError
import com.github.osxhacker.foundation.models.core.functional.ErrorOr


/**
 * The '''LanguageSpec''' type defines the unit tests which certify the
 * [[com.github.osxhacker.foundation.models.core.text.Language]] type for fitness of
 * purpose and serves as an exemplar for use.
 *
 * @author osxhacker
 *
 */
final class LanguageSpec
	extends ProjectSpec
		with DiagrammedAssertions
{
	/// Class Imports
	import format.textualize._
	import syntax.all._


	"A Language" when {
		"being created" must {
			"detect empty candidates" in {
				assert (Language ("").isLeft);
				}

			"detect malformed candidates" in {
				assert (Language ("foo-").isLeft);
				assert (Language ("-bar").isLeft);
				assert (Language ("foo- bar").isLeft);
				assert (Language ("x").isLeft);
				assert (Language ("exceedsLength").isLeft);
				assert (Language ("primary-bad").isLeft);
				}

			"accept well-formed candidates" in {
				assert (Language ("en-GB").isRight);
				}
			}

		"being compared" must {
			"ignore tag casing" in {
				assert (Language ("EN") == Language ("en"));
				assert (Language ("eN") == Language ("En"));
				assert (Language ("En") == Language ("En"));

				assert (Language ("fr-CA") == Language ("FR-ca"));
				}
			}

		"determining 'containment'" must {
			val british = Language ("en-GB") orThrow;
			val english = Language ("en") orThrow;
			val french = Language ("fr") orThrow;
			val us = Language ("en-US") orThrow;

			"use the region when present" in {
				assert (us.contains (british) === false);
				assert (us.contains (english) === false);
				assert (us.contains (us) === true);
				}

			"decide by primary language only with no reagion" in {
				assert (english.contains (us) === true);
				assert (english.contains (british) === true);
				assert (french.contains (us) === false);
				}
			}

		"determining ordering" must {
			"coallate based on the tag (ignoring case)" in {
				val english = Language ("En") orThrow;
				val french = Language ("FR") orThrow;
				val us = Language ("en-US") orThrow;

				assert (french.compare (us) > 0);
				assert (english.compare (us) < 0);
				assert (french.compare (french) === 0);
				assert (us.compare (us) === 0);
				}
			}

		"externalizing" must {
			"be in canonical form" in {
				assert (
					(Language ("EN-US") >>= (_.externalized[ErrorOr])) ==
					\/- ("en-US")
					);
				}

			"support 'Textualize'" in {
				val en = Language ("EN-GB") orThrow;

				assert (en.toText () == "en-GB");
				}
			}
		}
}
