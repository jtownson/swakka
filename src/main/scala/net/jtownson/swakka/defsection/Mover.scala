package net.jtownson.swakka.defsection

import com.jayway.jsonpath.{JsonPath, ReadContext}
import net.minidev.json.JSONArray
import spray.json.{JsArray, JsObject, JsString, JsValue, JsonParser}


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
