package net.jtownson.swakka

import net.jtownson.swakka.jsonprotocol.{PathsJsonProtocol, ParametersJsonProtocol, ResponsesJsonProtocol}
import spray.json.DefaultJsonProtocol

trait OpenApiJsonProtocol extends
  ParametersJsonProtocol with
  ResponsesJsonProtocol with
  PathsJsonProtocol with
  DefaultJsonProtocol

object OpenApiJsonProtocol extends OpenApiJsonProtocol