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
package net.jtownson.swakka.openapimodel

import shapeless.HNil

case class Header[T](name: Symbol, description: Option[String] = None)

case class ResponseValue[T, Headers](responseCode: String,
                                     description: String,
                                     headers: Headers)

object ResponseValue {
  def apply[T](responseCode: String,
               description: String)
  : ResponseValue[T, HNil] = ResponseValue(responseCode, description, HNil)
}