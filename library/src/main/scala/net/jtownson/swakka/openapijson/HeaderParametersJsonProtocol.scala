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
        None,
        enumOf(hp))

  implicit val strOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[String]]] =
    (hp: HeaderParameter[Option[String]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "string",
        None,
        defaultOf(hp),
        enumOfOption(hp))

  implicit val floatReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Float]] =
    (hp: HeaderParameter[Float]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "number",
        Some("float"),
        None,
        enumOf(hp))

  implicit val floatOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Float]]] =
    (hp: HeaderParameter[Option[Float]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "number",
        Some("float"),
        defaultOf(hp),
        enumOfOption(hp))

  implicit val doubleReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Double]] =
    (hp: HeaderParameter[Double]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "number",
        Some("double"),
        None,
        enumOf(hp))

  implicit val doubleOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Double]]] =
    (hp: HeaderParameter[Option[Double]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "number",
        Some("double"),
        defaultOf(hp),
        enumOfOption(hp))

  implicit val booleanReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Boolean]] =
    (hp: HeaderParameter[Boolean]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "boolean",
        None,
        None,
        enumOf(hp))

  implicit val booleanOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Boolean]]] =
    (hp: HeaderParameter[Option[Boolean]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "boolean",
        None,
        defaultOf(hp),
        enumOfOption(hp))

  implicit val intReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Int]] =
    (hp: HeaderParameter[Int]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "integer",
        Some("int32"),
        None,
        enumOf(hp))

  implicit val intOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Int]]] =
    (hp: HeaderParameter[Option[Int]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "integer",
        Some("int32"),
        defaultOf(hp),
        enumOfOption(hp))

  implicit val longReqHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Long]] =
    (hp: HeaderParameter[Long]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        true,
        "integer",
        Some("int64"),
        None,
        enumOf(hp))

  implicit val longOptHeaderParamFormat
  : ParameterJsonFormat[HeaderParameter[Option[Long]]] =
    (hp: HeaderParameter[Option[Long]]) =>
      simpleParam(hp.name,
        "header",
        hp.description,
        false,
        "integer",
        Some("int64"),
        defaultOf(hp),
        enumOfOption(hp))

}

object HeaderParametersJsonProtocol extends HeaderParametersJsonProtocol
