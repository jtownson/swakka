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

package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.OK
import net.jtownson.swakka.model.Parameters.{MultiValued, QueryParameter}
import org.scalatest.FlatSpec

class MultiParamConvertersTest extends FlatSpec with ConverterTest {

  "MultiParamConverters" should "convert multi query parameters" in {
    val mqp = MultiValued[String, QueryParameter[String]](QueryParameter[String](
      name = 'status,
      description = Some("Status values that need to be considered for filter"),
      default = Some("available"),
      enum = Some(Seq("available", "pending", "sold"))
    ))

    val request = Get(s"http://example.com?x=a1&x=a2")

    converterTest(request, mqp, OK, extractionAssertion(Seq("a1", "a2")))
  }
}
