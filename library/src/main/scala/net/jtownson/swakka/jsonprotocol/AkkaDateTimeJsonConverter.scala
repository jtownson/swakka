package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.model.DateTime
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

class DateTimeJsonConverter extends RootJsonFormat[DateTime] {

  override def write(dateTime: DateTime): JsValue =
    JsString(dateTime.toIsoDateTimeString())

  override def read(json: JsValue): DateTime = json match {
    case JsString(s) =>
      DateTime
        .fromIsoDateTimeString(s)
        .getOrElse(
          throw DeserializationException(
            s"Expected a iso formatted date time but got $s"))
    case v =>
      throw DeserializationException(
        s"Expected a iso formatted date time but got $v")

  }
}
