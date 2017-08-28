package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives.{parameter, _}
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter

trait QueryParamConverters {

  private def close[T](qp: QueryParameter[T]): T => QueryParameter[T] =
    t => qp.asInstanceOf[OpenQueryParameter[T]].closeWith(t)

  implicit val stringReqQueryConverter: ConvertibleToDirective[QueryParameter[String]] =
    (_: String, qp: QueryParameter[String]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.?(default)).map(close(qp))
        case None => parameter(qp.name).map(close(qp))
      }
    }

  implicit val stringOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[String]]] =
    (_: String, qp: QueryParameter[Option[String]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.?(default)).map(os => close(qp)(Some(os)))
        case _ => parameter(qp.name.?).map(close(qp))
      }
    }

  implicit val floatReqQueryConverter: ConvertibleToDirective[QueryParameter[Float]] =
    (_: String, qp: QueryParameter[Float]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Float].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Float]).map(close(qp))
      }
    }

  implicit val floatOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Float]]] =
    (_: String, qp: QueryParameter[Option[Float]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Float].?(default)).map(of => close(qp)(Some(of)))
        case _ => parameter(qp.name.as[Float].?).map(close(qp))
      }
    }

  implicit val doubleReqQueryConverter: ConvertibleToDirective[QueryParameter[Double]] =
    (_: String, qp: QueryParameter[Double]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Double].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Double]).map(close(qp))
      }
    }

  implicit val doubleOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Double]]] =
    (_: String, qp: QueryParameter[Option[Double]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Double].?(default)).map(dp => close(qp)(Some(dp)))
        case _ => parameter(qp.name.as[Double].?).map(close(qp))
      }
    }

  implicit val booleanReqQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] =
    (_: String, qp: QueryParameter[Boolean]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Boolean].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Boolean]).map(close(qp))
      }
    }

  implicit val booleanOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Boolean]]] =
    (_: String, qp: QueryParameter[Option[Boolean]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Boolean].?(default)).map(bp => close(qp)(Some(bp)))
        case _ => parameter(qp.name.as[Boolean].?).map(close(qp))
      }
    }

  implicit val intReqQueryConverter: ConvertibleToDirective[QueryParameter[Int]] =
    (_: String, qp: QueryParameter[Int]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Int].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Int]).map(close(qp))
      }
    }

  implicit val intOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Int]]] =
    (_: String, qp: QueryParameter[Option[Int]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Int].?(default)).map(ip => close(qp)(Some(ip)))
        case _ => parameter(qp.name.as[Int].?).map(close(qp))
      }
    }

  implicit val longReqQueryConverter: ConvertibleToDirective[QueryParameter[Long]] =
    (_: String, qp: QueryParameter[Long]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Long].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Long]).map(close(qp))
      }
    }

  implicit val longOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Long]]] =
    (_: String, qp: QueryParameter[Option[Long]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Long].?(default)).map(lp => close(qp)(Some(lp)))
        case _ => parameter(qp.name.as[Long].?).map(close(qp))
      }
    }
}
