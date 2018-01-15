package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapijson.ParameterTemplates._
import net.jtownson.swakka.openapimodel.PathParameterConstrained
import spray.json.{JsArray, JsBoolean, JsNumber, JsString}

trait PathParametersConstrainedJsonProtocol {

  implicit val strReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[String, String]] =
    (pp: PathParameterConstrained[String, String]) =>
      constrainedParam(
        name = pp.name,
        in = "path",
        description = pp.description,
        required = true,
        `type` = "string",
        format = None,
        default = None,
        enum = pp.constraints.enum.map(set => JsArray(set.map(JsString(_)).toVector)),
        minLength = pp.constraints.minLength.map(JsNumber(_)),
        maxLength = pp.constraints.maxLength.map(JsNumber(_)),
        pattern = pp.constraints.pattern.map(JsString(_)))

  implicit val floatReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Float, Float]] =
    (pp: PathParameterConstrained[Float, Float]) =>
      constrainedParam(name = pp.name,
        in = "path",
        description = pp.description,
        required = true,
        `type` = "number",
        format = Some("float"),
        default = None,
        enum = pp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = pp.constraints.maximum.map(JsNumber(_)),
        minimum = pp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = pp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = pp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val doubleReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Double, Double]] =
    (pp: PathParameterConstrained[Double, Double]) =>
      constrainedParam(name = pp.name,
        in = "path",
        description = pp.description,
        required = true,
        `type` = "number",
        format = Some("double"),
        default = None,
        enum = pp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = pp.constraints.maximum.map(JsNumber(_)),
        minimum = pp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = pp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = pp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val booleanReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Boolean, Boolean]] =
    (pp: PathParameterConstrained[Boolean, Boolean]) =>
      constrainedParam(name = pp.name,
        in = "path",
        description = pp.description,
        required = true,
        `type` = "boolean",
        format = None,
        default = None,
        enum = pp.constraints.enum.map(set => JsArray(set.map(JsBoolean(_)).toVector))
      )

  implicit val intReqPathParamFormatConstrained: ParameterJsonFormat[PathParameterConstrained[Int, Int]] =
    (pp: PathParameterConstrained[Int, Int]) =>
      constrainedParam(name = pp.name,
        in = "path",
        description = pp.description,
        required = true,
        `type` = "integer",
        format = Some("int32"),
        default = None,
        enum = pp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = pp.constraints.multipleOf.map(JsNumber(_)),
        maximum = pp.constraints.maximum.map(JsNumber(_)),
        minimum = pp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = pp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = pp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val longReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Long, Long]] =
    (pp: PathParameterConstrained[Long, Long]) =>
      constrainedParam(name = pp.name,
        in = "path",
        description = pp.description,
        required = true,
        `type` = "integer",
        format = Some("int64"),
        default = None,
        enum = pp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = pp.constraints.multipleOf.map(JsNumber(_)),
        maximum = pp.constraints.maximum.map(JsNumber(_)),
        minimum = pp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = pp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = pp.constraints.exclusiveMinimum.map(JsNumber(_)))
}

object PathParametersConstrainedJsonProtocol extends PathParametersConstrainedJsonProtocol
