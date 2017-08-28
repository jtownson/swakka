package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{as, entity, provide}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import net.jtownson.swakka.model.Parameters.BodyParameter
import net.jtownson.swakka.model.Parameters.BodyParameter.OpenBodyParameter

trait BodyParamConverters {

  implicit def bodyParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[T]] =
    (_: String, bp: BodyParameter[T]) => {
      bp.default match {
        case None => entity(as[T]).map(close(bp))
        case Some(default) => optionalEntity[T](as[T]).map {
          case Some(value) => close(bp)(value)
          case None => close(bp)(default)
        }
      }
    }

  implicit def bodyOptParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[Option[T]]] =
    (_: String, bp: BodyParameter[Option[T]]) => {
      bp.default match {
        case None => optionalEntity[T](as[T]).map(close(bp))
        case Some(default) => optionalEntity[T](as[T]).map {
          case v@Some(_) => close(bp)(v)
          case None => close(bp)(default)
        }
      }
    }

  def optionalEntity[T](unmarshaller: FromRequestUnmarshaller[T]): Directive1[Option[T]] =
    entity(as[String]).flatMap { stringEntity =>
      if (stringEntity == null || stringEntity.isEmpty) {
        provide(Option.empty[T])
      } else {
        entity(unmarshaller).flatMap(e => provide(Some(e)))
      }
    }

  private def close[T](bp: BodyParameter[T]): T => BodyParameter[T] =
    t => bp.asInstanceOf[OpenBodyParameter[T]].closeWith(t)

}
