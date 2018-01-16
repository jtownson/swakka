package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel.QueryParameter
import ParameterTemplates._

trait QueryParametersJsonProtocol {

  implicit val strReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[String]] =
    (qp: QueryParameter[String]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "string",
        None,
        None)

  implicit val strOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[String]]] =
    (qp: QueryParameter[Option[String]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "string",
        None,
        defaultOf(qp))

  implicit val floatReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Float]] =
    (qp: QueryParameter[Float]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "number",
        Some("float"),
        None)

  implicit val floatOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Float]]] =
    (qp: QueryParameter[Option[Float]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "number",
        Some("float"),
        defaultOf(qp))

  implicit val doubleReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Double]] =
    (qp: QueryParameter[Double]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "number",
        Some("double"),
        None)

  implicit val doubleOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Double]]] =
    (qp: QueryParameter[Option[Double]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "number",
        Some("double"),
        defaultOf(qp))

  implicit val booleanReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Boolean]] =
    (qp: QueryParameter[Boolean]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "boolean",
        None,
        None)

  implicit val booleanOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Boolean]]] =
    (qp: QueryParameter[Option[Boolean]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "boolean",
        None,
        defaultOf(qp))

  implicit val intReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Int]] =
    (qp: QueryParameter[Int]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "integer",
        Some("int32"),
        None)

  implicit val intOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Int]]] =
    (qp: QueryParameter[Option[Int]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "integer",
        Some("int32"),
        defaultOf(qp))

  implicit val longReqQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Long]] =
    (qp: QueryParameter[Long]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        true,
        "integer",
        Some("int64"),
        None)

  implicit val longOptQueryParamFormat
  : ParameterJsonFormat[QueryParameter[Option[Long]]] =
    (qp: QueryParameter[Option[Long]]) =>
      simpleParam(qp.name,
        "query",
        qp.description,
        false,
        "integer",
        Some("int64"),
        defaultOf(qp))
}

object QueryParametersJsonProtocol extends QueryParametersJsonProtocol
