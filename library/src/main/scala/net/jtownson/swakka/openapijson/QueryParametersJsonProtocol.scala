package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel.QueryParameter
import ParameterTemplates._
import net.jtownson.swakka.openapijson.ParameterJsonFormat.instance

trait QueryParametersJsonProtocol {

  implicit val strReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[String]] =
    instance((qp: QueryParameter[String]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "string",
        None,
        None))

  implicit val strOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[String]]] =
    instance((qp: QueryParameter[Option[String]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "string",
        None,
        defaultOf(qp)))

  implicit val floatReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Float]] =
    instance((qp: QueryParameter[Float]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "number",
        Some("float"),
        None))

  implicit val floatOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Float]]] =
    instance((qp: QueryParameter[Option[Float]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "number",
        Some("float"),
        defaultOf(qp)))

  implicit val doubleReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Double]] =
    instance((qp: QueryParameter[Double]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "number",
        Some("double"),
        None))

  implicit val doubleOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Double]]] =
    instance((qp: QueryParameter[Option[Double]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "number",
        Some("double"),
        defaultOf(qp)))

  implicit val booleanReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Boolean]] =
    instance((qp: QueryParameter[Boolean]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "boolean",
        None,
        None))

  implicit val booleanOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Boolean]]] =
    instance((qp: QueryParameter[Option[Boolean]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "boolean",
        None,
        defaultOf(qp)))

  implicit val intReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Int]] =
    instance((qp: QueryParameter[Int]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "integer",
        Some("int32"),
        None))

  implicit val intOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Int]]] =
    instance((qp: QueryParameter[Option[Int]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "integer",
        Some("int32"),
        defaultOf(qp)))

  implicit val longReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Long]] =
    instance((qp: QueryParameter[Long]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "integer",
        Some("int64"),
        None))

  implicit val longOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Long]]] =
    instance((qp: QueryParameter[Option[Long]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "integer",
        Some("int64"),
        defaultOf(qp)))
}

object QueryParametersJsonProtocol extends QueryParametersJsonProtocol
