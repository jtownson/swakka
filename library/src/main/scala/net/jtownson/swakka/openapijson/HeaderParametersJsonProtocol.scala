package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel.HeaderParameter
import ParameterTemplates._

trait HeaderParametersJsonProtocol {
  implicit val strReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[String]] =
    (hp: HeaderParameter[String]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "string",
        None,
        None)

  implicit val strOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[String]]] =
    (hp: HeaderParameter[Option[String]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "string",
        None,
        defaultOf(hp))

  implicit val floatReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Float]] =
    (hp: HeaderParameter[Float]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "number",
        Some("float"),
        None)

  implicit val floatOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Float]]] =
    (hp: HeaderParameter[Option[Float]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "number",
        Some("float"),
        defaultOf(hp))

  implicit val doubleReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Double]] =
    (hp: HeaderParameter[Double]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "number",
        Some("double"),
        None)

  implicit val doubleOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Double]]] =
    (hp: HeaderParameter[Option[Double]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "number",
        Some("double"),
        defaultOf(hp))

  implicit val booleanReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Boolean]] =
    (hp: HeaderParameter[Boolean]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "boolean",
        None,
        None)

  implicit val booleanOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Boolean]]] =
    (hp: HeaderParameter[Option[Boolean]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "boolean",
        None,
        defaultOf(hp))

  implicit val intReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Int]] =
    (hp: HeaderParameter[Int]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "integer",
        Some("int32"),
        None)

  implicit val intOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Int]]] =
    (hp: HeaderParameter[Option[Int]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "integer",
        Some("int32"),
        defaultOf(hp))

  implicit val longReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Long]] =
    (hp: HeaderParameter[Long]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "integer",
        Some("int64"),
        None)

  implicit val longOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Long]]] =
    (hp: HeaderParameter[Option[Long]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "integer",
        Some("int64"),
        defaultOf(hp))

}

object HeaderParametersJsonProtocol extends HeaderParametersJsonProtocol
