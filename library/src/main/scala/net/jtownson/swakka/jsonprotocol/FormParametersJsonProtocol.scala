package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonprotocol.ParameterJsonFormat.func2Format
import net.jtownson.swakka.jsonschema.ApiModelDictionary.{apiModelDictionary, apiModelKeys}
import net.jtownson.swakka.jsonschema.ApiModelPropertyEntry
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.model.Parameters.FormParameter
import spray.json.{JsArray, JsBoolean, JsString, JsValue}

import scala.reflect.runtime.universe.TypeTag

trait FormParametersJsonProtocol {

  def requiredFormParameterFormat[P1, T <: Product : TypeTag](ctor: P1 => T)
  (implicit ef1: FormParameterType[P1]): ParameterJsonFormat[FormParameter[P1, T]] = {

    val tDictionary: Map[String, ApiModelPropertyEntry] = apiModelDictionary[T]
    val fields: Seq[String] = apiModelKeys[T]

    func2Format((_: FormParameter[P1, T]) => {

      val fieldName = fields(0)
      val f1Entry = tDictionary(fieldName)

      formDataItem(fieldName, f1Entry.value, f1Entry.required, ef1.swaggerType, ef1.swaggerFormat)
    })
  }

  def requiredFormParameterFormat[P1, P2, T <: Product : TypeTag](ctor: (P1, P2) => T)
  (implicit ef1: FormParameterType[P1], ef2: FormParameterType[P2]): ParameterJsonFormat[FormParameter[(P1, P2), T]] = {

    val tDictionary: Map[String, ApiModelPropertyEntry] = apiModelDictionary[T]
    val fields: Seq[String] = apiModelKeys[T]

    func2Format((_: FormParameter[(P1, P2), T]) => {

      val (f1, f2) = (fields(0), fields(1))
      val (f1Entry, f2Entry) = (tDictionary(f1), tDictionary(f2))

      JsArray(
        formDataItem(f1, f1Entry.value, f1Entry.required, ef1.swaggerType, ef1.swaggerFormat),
        formDataItem(f2, f2Entry.value, f2Entry.required, ef2.swaggerType, ef2.swaggerFormat)
      )
    })
  }

  def requiredFormParameterFormat[P1, P2, P3, T <: Product : TypeTag](ctor: (P1, P2, P3) => T)
  (implicit ef1: FormParameterType[P1], ef2: FormParameterType[P2], ef3: FormParameterType[P3]):
  ParameterJsonFormat[FormParameter[(P1, P2, P3), T]] = {

    val tDictionary: Map[String, ApiModelPropertyEntry] = apiModelDictionary[T]
    val fields: Seq[String] = apiModelKeys[T]

    func2Format((_: FormParameter[(P1, P2, P3), T]) => {

      val (f1, f2, f3) = (fields(0), fields(1), fields(2))
      val (f1Entry, f2Entry, f3Entry) = (tDictionary(f1), tDictionary(f2), tDictionary(f3))

      JsArray(
        formDataItem(f1, f1Entry.value, f1Entry.required, ef1.swaggerType, ef1.swaggerFormat),
        formDataItem(f2, f2Entry.value, f2Entry.required, ef2.swaggerType, ef2.swaggerFormat),
        formDataItem(f3, f3Entry.value, f3Entry.required, ef3.swaggerType, ef3.swaggerFormat)
      )
    })
  }

  private def formDataItem(name: String, description: Option[String], required: Boolean, `type`: String, format: Option[String]): JsValue = {
    jsObject(
      Some("name" -> JsString(name)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      Some("in" -> JsString("formData")),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required))
    )
  }


}
