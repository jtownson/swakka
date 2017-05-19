package net.jtownson.swakka

import net.jtownson.swakka.jsonprotocol.{HeadersJsonProtocol, ParametersJsonProtocol, PathsJsonProtocol, ResponsesJsonProtocol}
import spray.json.DefaultJsonProtocol

trait OpenApiJsonProtocol extends
  ParametersJsonProtocol with
  ResponsesJsonProtocol with
  PathsJsonProtocol with
  HeadersJsonProtocol with
  DefaultJsonProtocol

object OpenApiJsonProtocol extends OpenApiJsonProtocol