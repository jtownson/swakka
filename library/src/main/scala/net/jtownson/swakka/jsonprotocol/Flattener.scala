package net.jtownson.swakka.jsonprotocol

import spray.json.{JsArray, JsObject, JsValue}


object Flattener {

  def flattenToArray(jsValue: JsArray): JsArray = jsValue match {
    case JsArray(Vector(head, JsArray(tail: Seq[JsValue]))) =>
      head match {
        case JsArray(headFields: Seq[JsValue]) =>
          JsArray(headFields.toList ++ tail.toList: _*)
        case _ => JsArray(head :: tail.toList: _*)
      }
    case _: JsValue =>
      jsValue
  }

  val toKeyVal: JsValue => Seq[(String, JsValue)] = {
    case JsObject(fields) => fields.toList
    case _ => Nil
  }

  private val mergeValues: (JsValue, JsValue) => JsValue = (acc, nextJsValue) => {
    (acc, nextJsValue) match {
      case (JsObject(accFields), JsObject(nextFields)) => JsObject(accFields ++ nextFields)
      case _ => acc
    }
  }


  def merge(extractedFields: Seq[(String, JsValue)]): Map[String, JsValue] = {

    val groupBy: Map[String, Seq[(String, JsValue)]] = extractedFields.groupBy(_._1)

    groupBy.map(t => {
      val (key, vals: Seq[(String, JsValue)]) = t
      if (vals.size == 1)
        vals.head
      else
        (key, vals.map(_._2).reduceLeft(mergeValues))
    })
  }

  def flattenToObject(jsValue: JsArray): JsObject = {
    val arrayOfFields: Seq[JsValue] = flattenToArray(jsValue).elements

    val extractedFields: Seq[(String, JsValue)] = arrayOfFields flatMap toKeyVal

    val mergedFields: Map[String, JsValue] = merge(extractedFields)

    JsObject(mergedFields)
  }
}
