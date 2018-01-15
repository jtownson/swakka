package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel.QueryParameterConstrained
import ParameterTemplates._
import spray.json.{JsArray, JsBoolean, JsNumber, JsString}

trait QueryParametersConstrainedJsonProtocol {

  implicit val strReqQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[String, String]] =
    (qp: QueryParameterConstrained[String, String]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = true,
        `type` = "string",
        format = None,
        default = None,
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsString(_)).toVector)),
        minLength = qp.constraints.minLength.map(JsNumber(_)),
        maxLength = qp.constraints.maxLength.map(JsNumber(_)),
        pattern = qp.constraints.pattern.map(JsString(_)))

  implicit val floatReqQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Float, Float]] =
    (qp: QueryParameterConstrained[Float, Float]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = true,
        `type` = "number",
        format = Some("float"),
        default = None,
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val doubleReqQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Double, Double]] =
    (qp: QueryParameterConstrained[Double, Double]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = true,
        `type` = "number",
        format = Some("double"),
        default = None,
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val booleanReqQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Boolean, Boolean]] =
    (qp: QueryParameterConstrained[Boolean, Boolean]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = true,
        `type` = "boolean",
        format = None,
        default = None,
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsBoolean(_)).toVector)))

  implicit val intReqQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Int, Int]] =
    (qp: QueryParameterConstrained[Int, Int]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = true,
        `type` = "integer",
        format = Some("int32"),
        default = None,
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = qp.constraints.multipleOf.map(JsNumber(_)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val longReqQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Long, Long]] =
    (qp: QueryParameterConstrained[Long, Long]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = true,
        `type` = "integer",
        format = Some("int64"),
        default = None,
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = qp.constraints.multipleOf.map(JsNumber(_)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val strOptQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Option[String], String]] =
    (qp: QueryParameterConstrained[Option[String], String]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = false,
        `type` = "string",
        format = None,
        default = defaultOf(qp),
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsString(_)).toVector)),
        minLength = qp.constraints.minLength.map(JsNumber(_)),
        maxLength = qp.constraints.maxLength.map(JsNumber(_)),
        pattern = qp.constraints.pattern.map(JsString(_)))

  implicit val floatOptQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Option[Float], Float]] =
    (qp: QueryParameterConstrained[Option[Float], Float]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = false,
        `type` = "number",
        format = Some("float"),
        default = defaultOf(qp),
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val doubleOptQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Option[Double], Double]] =
    (qp: QueryParameterConstrained[Option[Double], Double]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = false,
        `type` = "number",
        format = Some("double"),
        default = defaultOf(qp),
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val booleanOptQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Option[Boolean], Boolean]] =
    (qp: QueryParameterConstrained[Option[Boolean], Boolean]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = false,
        `type` = "boolean",
        format = None,
        default = defaultOf(qp),
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsBoolean(_)).toVector)))

  implicit val intOptQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Option[Int], Int]] =
    (qp: QueryParameterConstrained[Option[Int], Int]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = false,
        `type` = "integer",
        format = Some("int32"),
        default = defaultOf(qp),
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = qp.constraints.multipleOf.map(JsNumber(_)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))

  implicit val longOptQueryParamFormatConstrained
  : ParameterJsonFormat[QueryParameterConstrained[Option[Long], Long]] =
    (qp: QueryParameterConstrained[Option[Long], Long]) =>
      constrainedParam(name = qp.name,
        in = "query",
        description = qp.description,
        required = false,
        `type` = "integer",
        format = Some("int64"),
        default = defaultOf(qp),
        enum = qp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = qp.constraints.multipleOf.map(JsNumber(_)),
        maximum = qp.constraints.maximum.map(JsNumber(_)),
        minimum = qp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = qp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = qp.constraints.exclusiveMinimum.map(JsNumber(_)))
}

object QueryParametersConstrainedJsonProtocol extends QueryParametersConstrainedJsonProtocol
