package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives.{headerValueByName, optionalHeaderValueByName}
import net.jtownson.swakka.model.Parameters.HeaderParameter
import net.jtownson.swakka.model.Parameters.HeaderParameter.OpenHeaderParameter

trait HeaderParamConverters {

  implicit val stringReqHeaderConverter: ConvertibleToDirective[HeaderParameter[String]] =
    requiredHeaderParamDirective(s => s)

  implicit val stringOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[String]]] =
    optionalHeaderParamDirective(s => s)

  implicit val floatReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Float]] =
    requiredHeaderParamDirective(_.toFloat)

  implicit val floatOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Float]]] =
    optionalHeaderParamDirective(_.toFloat)

  implicit val doubleReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Double]] =
    requiredHeaderParamDirective(_.toDouble)

  implicit val doubleOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Double]]] =
    optionalHeaderParamDirective(_.toDouble)

  implicit val booleanReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Boolean]] =
    requiredHeaderParamDirective(_.toBoolean)

  implicit val booleanOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Boolean]]] =
    optionalHeaderParamDirective(_.toBoolean)

  implicit val intReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Int]] =
    requiredHeaderParamDirective(_.toInt)

  implicit val intOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Int]]] =
    optionalHeaderParamDirective(_.toInt)

  implicit val longReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Long]] =
    requiredHeaderParamDirective(_.toLong)

  implicit val longOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Long]]] =
    optionalHeaderParamDirective(_.toLong)

  private def requiredHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[T]] = (_: String, hp: HeaderParameter[T]) => {
    hp.default match {
      case Some(default) => optionalHeaderValueByName(hp.name).map {
        case Some(header) => close(hp)(valueParser(header))
        case None => close(hp)(default)
      }
      case None => headerValueByName(hp.name).map(value => close(hp)(valueParser(value)))
    }
  }

  private def optionalHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[Option[T]]] = (_: String, hp: HeaderParameter[Option[T]]) => {

    hp.default match {
      case Some(default) =>
        optionalHeaderValueByName(hp.name).map {
          case Some(header) => close(hp)(Some(valueParser(header)))
          case None => close(hp)(default)
        }
      case None =>
        optionalHeaderValueByName(hp.name).map {
          case Some(value) => close(hp)(Some(valueParser(value)))
          case None => close(hp)(None)
        }
    }
  }

  private def close[T](hp: HeaderParameter[T]): T => HeaderParameter[T] =
    t => hp.asInstanceOf[OpenHeaderParameter[T]].closeWith(t)

}
