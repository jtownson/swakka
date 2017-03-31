package net.jtownson.minimal

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import net.jtownson.minimal.MinimalOpenApiModel.QueryParameter
import shapeless.HList

trait ConvertibleToDirective0[T] {
  def convert(t: T): Directive0
}

object ConvertibleToDirective0 {

  import shapeless.{::, HNil}

  def convertToDirective0[T](t: T)(implicit convertible: ConvertibleToDirective0[T]): Directive0 =
    convertible.convert(t)

  def instance[T](f: T => Directive0) = new ConvertibleToDirective0[T] {
    def convert(t: T): Directive0 = f(t)
  }

  implicit val stringQueryConverter: ConvertibleToDirective0[QueryParameter[String]] =
    instance(qp => parameter(qp.name).tmap(_ => ()))

  implicit val intQueryConverter: ConvertibleToDirective0[QueryParameter[Int]] =
    instance(qp => parameter(qp.name.as[Int]).tmap(_ => ()))

  implicit val hNilConverter: ConvertibleToDirective0[HNil] = _ => pass

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective0[H],
                                             tail: ConvertibleToDirective0[T]):
  ConvertibleToDirective0[H :: T] =
    instance[H :: T](l => head.convert(l.head) & tail.convert(l.tail))

}