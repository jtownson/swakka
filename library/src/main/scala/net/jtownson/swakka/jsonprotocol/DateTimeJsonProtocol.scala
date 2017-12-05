package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.model.DateTime
import spray.json.RootJsonFormat

trait DateTimeJsonConverters {
  implicit val akkaDateTimeJsonConverter: RootJsonFormat[DateTime] = new AkkaDateTimeJsonConverter()
}

object DateTimeJsonConverters extends DateTimeJsonConverters
