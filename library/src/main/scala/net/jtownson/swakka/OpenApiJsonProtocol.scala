package net.jtownson.swakka

import net.jtownson.swakka.jsonprotocol._
import spray.json.DefaultJsonProtocol

trait OpenApiJsonProtocol extends
  ParametersJsonProtocol with
  ResponsesJsonProtocol with
  PathsJsonProtocol with
  HeadersJsonProtocol with
  SecurityDefinitionsJsonProtocol with
  DefaultJsonProtocol

object OpenApiJsonProtocol extends OpenApiJsonProtocol