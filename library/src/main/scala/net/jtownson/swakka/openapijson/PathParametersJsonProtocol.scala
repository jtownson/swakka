package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel.PathParameter
import ParameterTemplates._

trait PathParametersJsonProtocol {

  implicit val strReqPathParamFormat
  : ParameterJsonFormat[PathParameter[String]] =
    (pp: PathParameter[String]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "string",
        None,
        None,
        enumOf(pp))

  implicit val floatReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Float]] =
    (pp: PathParameter[Float]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("float"),
        None,
        enumOf(pp))

  implicit val doubleReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Double]] =
    (pp: PathParameter[Double]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("double"),
        None,
        enumOf(pp))

  implicit val booleanReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Boolean]] =
    (pp: PathParameter[Boolean]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "boolean",
        None,
        None,
        enumOf(pp))

  implicit val intReqPathParamFormat: ParameterJsonFormat[PathParameter[Int]] =
    (pp: PathParameter[Int]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int32"),
        None,
        enumOf(pp))

  implicit val longReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Long]] =
    (pp: PathParameter[Long]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int64"),
        None,
        enumOf(pp))


}

object PathParametersJsonProtocol extends PathParametersJsonProtocol
