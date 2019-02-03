package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel.PathParameter
import ParameterTemplates._
import net.jtownson.swakka.openapijson.ParameterJsonFormat.instance

trait PathParametersJsonProtocol {

  implicit val strReqPathParamFormat
  : ParameterJsonFormat[PathParameter[String]] =
    instance((pp: PathParameter[String]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "string",
        None,
        None))

  implicit val floatReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Float]] =
    instance((pp: PathParameter[Float]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("float"),
        None))

  implicit val doubleReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Double]] =
    instance((pp: PathParameter[Double]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "number",
        Some("double"),
        None))

  implicit val booleanReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Boolean]] =
    instance((pp: PathParameter[Boolean]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "boolean",
        None,
        None))

  implicit val intReqPathParamFormat: ParameterJsonFormat[PathParameter[Int]] =
    instance((pp: PathParameter[Int]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int32"),
        None))

  implicit val longReqPathParamFormat
  : ParameterJsonFormat[PathParameter[Long]] =
    instance((pp: PathParameter[Long]) =>
      simpleParam(pp.name,
        "path",
        pp.description,
        true,
        "integer",
        Some("int64"),
        None))


}

object PathParametersJsonProtocol extends PathParametersJsonProtocol
