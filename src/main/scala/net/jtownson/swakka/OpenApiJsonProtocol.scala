package net.jtownson.swakka

import net.jtownson.swakka.jsonprotocol.{EndpointsJsonProtocol, ParametersJsonProtocol, ResponsesJsonProtocol}
import spray.json.DefaultJsonProtocol

trait OpenApiJsonProtocol extends
  ParametersJsonProtocol with
  ResponsesJsonProtocol with
  EndpointsJsonProtocol with
  DefaultJsonProtocol

object OpenApiJsonProtocol extends OpenApiJsonProtocol