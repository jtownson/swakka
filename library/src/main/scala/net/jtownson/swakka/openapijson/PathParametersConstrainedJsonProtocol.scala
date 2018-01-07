package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapijson.ParameterTemplates._
import net.jtownson.swakka.openapimodel.PathParameterConstrained
import spray.json.{JsNumber, JsString}

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
        minLength = pp.constraints.minLength.map(JsNumber(_)),
        maxLength = pp.constraints.maxLength.map(JsNumber(_)),
        pattern = pp.constraints.pattern.map(JsString(_)))

  implicit val floatReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Float, Float]] =
    (pp: PathParameterConstrained[Float, Float]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("float"),
        None)

  implicit val doubleReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Double, Double]] =
    (pp: PathParameterConstrained[Double, Double]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("double"),
        None)

  implicit val booleanReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Boolean, Boolean]] =
    (pp: PathParameterConstrained[Boolean, Boolean]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "boolean",
        None,
        None)

  implicit val intReqPathParamFormatConstrained: ParameterJsonFormat[PathParameterConstrained[Int, Int]] =
    (pp: PathParameterConstrained[Int, Int]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int32"),
        None)

  implicit val longReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Long, Long]] =
    (pp: PathParameterConstrained[Long, Long]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int64"),
        None)
}

object PathParametersConstrainedJsonProtocol extends PathParametersConstrainedJsonProtocol
