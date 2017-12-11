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

package net.jtownson.swakka.defsection

import com.jayway.jsonpath.{JsonPath, ReadContext}
import net.minidev.json.JSONArray
import spray.json.{JsArray, JsObject, JsString, JsValue, JsonParser}

/**
  * TODO: Get this working. It should factorise a swagger document with duplicated
  * schema definitions into a swagger document containing a definitions section,
  * with each schema written once. The original schema locations should be replaced
  * by refs.
  */
object Mover {

  def move(v: JsValue): JsValue = {

    val schemaDefs: JsArray = getSchemaDefs(v)

    val defFields: Seq[(String, JsValue)] = schemaDefs.elements.map(jsValue => {
      val id: JsString = jsValue.asInstanceOf[JsObject].fields("id").asInstanceOf[JsString]
      (id.value, jsValue)
    })

    JsObject(v.asInstanceOf[JsObject].fields + ("definitions" -> JsObject(defFields: _*)))
  }

  private def getSchemaDefs(v: JsValue) = {
    val schemasJp = "$.paths.*.*.responses.*.schema"

    val json: String = v.compactPrint

    val ctx: ReadContext = JsonPath.parse(json)

    val res = ctx.read(schemasJp, classOf[JSONArray])

    JsonParser(res.toJSONString).asInstanceOf[JsArray]
  }
}
