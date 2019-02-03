package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel.HeaderParameterConstrained
import ParameterTemplates._
import net.jtownson.swakka.openapijson.ParameterJsonFormat.instance
import spray.json.{JsArray, JsBoolean, JsNumber, JsString}

trait HeaderParametersConstrainedJsonProtocol {

  implicit val strReqHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[String, String]] =
    instance((hp: HeaderParameterConstrained[String, String]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = true,
        `type` = "string",
        format = None,
        default = None,
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsString(_)).toVector)),
        minLength = hp.constraints.minLength.map(JsNumber(_)),
        maxLength = hp.constraints.maxLength.map(JsNumber(_)),
        pattern = hp.constraints.pattern.map(JsString(_))))

  implicit val floatReqHeaderParamFormatConstrained
        : ParameterJsonFormat[HeaderParameterConstrained[Float, Float]] =
    instance((hp: HeaderParameterConstrained[Float, Float]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = true,
        `type` = "number",
        format = Some("float"),
        default = None,
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))

  implicit val doubleReqHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Double, Double]] =
    instance((hp: HeaderParameterConstrained[Double, Double]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = true,
        `type` = "number",
        format = Some("double"),
        default = None,
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))

  implicit val booleanReqHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Boolean, Boolean]] =
    instance((hp: HeaderParameterConstrained[Boolean, Boolean]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = true,
        `type` = "boolean",
        format = None,
        default = None,
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsBoolean(_)).toVector))))

  implicit val intReqHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Int, Int]] =
    instance((hp: HeaderParameterConstrained[Int, Int]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = true,
        `type` = "integer",
        format = Some("int32"),
        default = None,
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = hp.constraints.multipleOf.map(JsNumber(_)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))

  implicit val longReqHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Long, Long]] =
    instance((hp: HeaderParameterConstrained[Long, Long]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = true,
        `type` = "integer",
        format = Some("int64"),
        default = None,
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = hp.constraints.multipleOf.map(JsNumber(_)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))

  implicit val strOptHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Option[String], String]] =
    instance((hp: HeaderParameterConstrained[Option[String], String]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = false,
        `type` = "string",
        format = None,
        default = defaultOf(hp),
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsString(_)).toVector)),
        minLength = hp.constraints.minLength.map(JsNumber(_)),
        maxLength = hp.constraints.maxLength.map(JsNumber(_)),
        pattern = hp.constraints.pattern.map(JsString(_))))

  implicit val floatOptHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Option[Float], Float]] =
    instance((hp: HeaderParameterConstrained[Option[Float], Float]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = false,
        `type` = "number",
        format = Some("float"),
        default = defaultOf(hp),
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))

  implicit val doubleOptHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Option[Double], Double]] =
    instance((hp: HeaderParameterConstrained[Option[Double], Double]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = false,
        `type` = "number",
        format = Some("double"),
        default = defaultOf(hp),
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))

  implicit val booleanOptHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Option[Boolean], Boolean]] =
    instance((hp: HeaderParameterConstrained[Option[Boolean], Boolean]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = false,
        `type` = "boolean",
        format = None,
        default = defaultOf(hp),
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsBoolean(_)).toVector))))

  implicit val intOptHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Option[Int], Int]] =
    instance((hp: HeaderParameterConstrained[Option[Int], Int]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = false,
        `type` = "integer",
        format = Some("int32"),
        default = defaultOf(hp),
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = hp.constraints.multipleOf.map(JsNumber(_)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))

  implicit val longOptHeaderParamFormatConstrained
  : ParameterJsonFormat[HeaderParameterConstrained[Option[Long], Long]] =
    instance((hp: HeaderParameterConstrained[Option[Long], Long]) =>
      constrainedParam(name = hp.name,
        in = "header",
        description = hp.description,
        required = false,
        `type` = "integer",
        format = Some("int64"),
        default = defaultOf(hp),
        enum = hp.constraints.enum.map(set => JsArray(set.map(JsNumber(_)).toVector)),
        multipleOf = hp.constraints.multipleOf.map(JsNumber(_)),
        maximum = hp.constraints.maximum.map(JsNumber(_)),
        minimum = hp.constraints.minimum.map(JsNumber(_)),
        exclusiveMaximum = hp.constraints.exclusiveMaximum.map(JsNumber(_)),
        exclusiveMinimum = hp.constraints.exclusiveMinimum.map(JsNumber(_))))
}

object HeaderParametersConstrainedJsonProtocol extends HeaderParametersConstrainedJsonProtocol
