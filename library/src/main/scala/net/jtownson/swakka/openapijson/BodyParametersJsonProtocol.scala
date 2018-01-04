package net.jtownson.swakka.openapijson

import net.jtownson.swakka.jsonschema.SchemaWriter
import net.jtownson.swakka.openapijson.ParameterJsonFormat.instance
import net.jtownson.swakka.openapimodel.BodyParameter
import spray.json.JsonWriter
import ParameterTemplates._

trait BodyParametersJsonProtocol {

  implicit def requiredBodyParamFormat[T](
      implicit ev: SchemaWriter[T]): ParameterJsonFormat[BodyParameter[T]] =
    instance((bp: BodyParameter[T]) =>
      bodyParam(ev, bp.name, bp.description, true, None))

  implicit def optionalBodyParamFormat[T: JsonWriter](
      implicit ev: SchemaWriter[T])
    : ParameterJsonFormat[BodyParameter[Option[T]]] =
    instance((bp: BodyParameter[Option[T]]) =>
      bodyParam(ev, bp.name, bp.description, false, defaultOf(bp)))

}

object BodyParametersJsonProtocol extends BodyParametersJsonProtocol
