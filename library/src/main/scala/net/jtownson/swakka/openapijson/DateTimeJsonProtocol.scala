package net.jtownson.swakka.openapijson

import akka.http.scaladsl.model.DateTime
import spray.json.RootJsonFormat

trait DateTimeJsonProtocol {
  implicit val akkaDateTimeJsonConverter: RootJsonFormat[DateTime] = new AkkaDateTimeJsonConverter()
}

object DateTimeJsonProtocol extends DateTimeJsonProtocol
