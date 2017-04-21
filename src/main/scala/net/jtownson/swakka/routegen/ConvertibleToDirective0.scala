package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import net.jtownson.swakka.OpenApiModel.{BodyParameter, QueryParameter}
import shapeless.HList

trait ConvertibleToDirective0[T] {
  def convertToDirective0(t: T): Directive0
}

object ConvertibleToDirective0 {

  import shapeless.{::, HNil}

  def instance[T](f: T => Directive0) = new ConvertibleToDirective0[T] {
    def convertToDirective0(t: T): Directive0 = f(t)
  }

  implicit val stringQueryConverter: ConvertibleToDirective0[QueryParameter[String]] =
    instance(qp => parameter(qp.name).tmap(_ => ()))

  implicit val intQueryConverter: ConvertibleToDirective0[QueryParameter[Int]] =
    instance(qp => parameter(qp.name.as[Int]).tmap(_ => ()))

  implicit def bodyParamConverter[T: FromRequestUnmarshaller]: ConvertibleToDirective0[BodyParameter[T]] =
    instance(_ => entity(as[T]).tmap(_ => ()))

  implicit val hNilConverter: ConvertibleToDirective0[HNil] = _ => pass

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective0[H],
                                             tail: ConvertibleToDirective0[T]):
  ConvertibleToDirective0[H :: T] =
    instance[H :: T](l => head.convertToDirective0(l.head) & tail.convertToDirective0(l.tail))

}