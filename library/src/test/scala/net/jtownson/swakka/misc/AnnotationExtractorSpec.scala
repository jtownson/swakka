/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jtownson.swakka.misc

import io.swagger.annotations.ApiModelProperty
import net.jtownson.swakka.misc.AnnotationExtractor._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class AnnotationExtractorSpec extends FlatSpec {

  case class D(
                @ApiModelProperty(value = "field 1", notes = "notes 1", required = true) id: Int,
                @ApiModelProperty(value = "field 2", notes = "notes 2") data: String
              )

  it should "find swagger annotations on a class" in {

    val annotationClass = classOf[ApiModelProperty]

    constructorAnnotations[D](annotationClass) shouldBe Map(
      "id" -> Set(("value", "field 1"), ("notes", "notes 1"), ("required", "true")),
      "data" -> Set(("value", "field 2"), ("notes", "notes 2"))
    )
  }
}
