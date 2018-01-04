package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapijson.ParameterTemplates._
import net.jtownson.swakka.openapimodel.{NumericValidationConstraints, PathParameterConstrained, StringValidationConstraints}
import spray.json.{JsNumber, JsString}

trait PathParametersConstrainedJsonProtocol {

  implicit val strReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[String, String, StringValidationConstraints]] =
    (pp: PathParameterConstrained[String, String, StringValidationConstraints]) =>
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
  : ParameterJsonFormat[PathParameterConstrained[Float, Float, NumericValidationConstraints[Float]]] =
    (pp: PathParameterConstrained[Float, Float, NumericValidationConstraints[Float]]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("float"),
        None)

  implicit val doubleReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Double, Double, NumericValidationConstraints[Double]]] =
    (pp: PathParameterConstrained[Double, Double, NumericValidationConstraints[Double]]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("double"),
        None)

  implicit val booleanReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Boolean, Boolean, NumericValidationConstraints[Boolean]]] =
    (pp: PathParameterConstrained[Boolean, Boolean, NumericValidationConstraints[Boolean]]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "boolean",
        None,
        None)

  implicit val intReqPathParamFormatConstrained: ParameterJsonFormat[PathParameterConstrained[Int, Int, NumericValidationConstraints[Int]]] =
    (pp: PathParameterConstrained[Int, Int, NumericValidationConstraints[Int]]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int32"),
        None)

  implicit val longReqPathParamFormatConstrained
  : ParameterJsonFormat[PathParameterConstrained[Long, Long, NumericValidationConstraints[Long]]] =
    (pp: PathParameterConstrained[Long, Long, NumericValidationConstraints[Long]]) =>
      constrainedParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int64"),
        None)
}

object PathParametersConstrainedJsonProtocol extends PathParametersConstrainedJsonProtocol
