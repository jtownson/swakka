package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import net.jtownson.swakka.OpenApiModel.{BodyParameter, PathParameter, QueryParameter}
import shapeless.HList


trait ConvertibleToDirective0[T] {
  def convertToDirective0(t: T): Directive0

  def paramMap(t: T): Map[String, PathMatcher0]
}

object ConvertibleToDirective0 {

  import shapeless.{::, HNil}

  private def instance[T](f: T => Directive0): ConvertibleToDirective0[T] =
    instance(f, _ => Map())

  private def instance[T](f: T => Directive0, g: T => Map[String, PathMatcher0]): ConvertibleToDirective0[T] =
    new ConvertibleToDirective0[T] {
      def convertToDirective0(t: T): Directive0 = f(t)
      def paramMap(t: T): Map[String, PathMatcher0] = g(t)
    }

  private val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""true|false""".r) flatMap (s => Some(s.toBoolean))

  private val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string ⇒
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }

  implicit val stringQueryConverter: ConvertibleToDirective0[QueryParameter[String]] =
    instance(qp => toDir0(parameter(qp.name)))

  implicit val floatQueryConverter: ConvertibleToDirective0[QueryParameter[Float]] =
    instance(qp => toDir0(parameter(qp.name.as[Float])))

  implicit val doubleQueryConverter: ConvertibleToDirective0[QueryParameter[Double]] =
    instance(qp => toDir0(parameter(qp.name.as[Double])))

  implicit val booleanQueryConverter: ConvertibleToDirective0[QueryParameter[Boolean]] =
    instance(qp => toDir0(parameter(qp.name.as[Boolean])))

  implicit val intQueryConverter: ConvertibleToDirective0[QueryParameter[Int]] =
    instance(qp => toDir0(parameter(qp.name.as[Int])))

  implicit val longQueryConverter: ConvertibleToDirective0[QueryParameter[Long]] =
    instance(qp => toDir0(parameter(qp.name.as[Long])))

  implicit val stringPathConverter: ConvertibleToDirective0[PathParameter[String]] =
    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(Segment)))

  implicit val floatPathConverter: ConvertibleToDirective0[PathParameter[Float]] =
    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(FloatNumber)))

  implicit val doublePathConverter: ConvertibleToDirective0[PathParameter[Double]] =
    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(DoubleNumber)))

  implicit val booleanPathConverter: ConvertibleToDirective0[PathParameter[Boolean]] =
    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(BooleanSegment)))

  implicit val intPathConverter: ConvertibleToDirective0[PathParameter[Int]] =
    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(IntNumber)))

  implicit val longPathConverter: ConvertibleToDirective0[PathParameter[Long]] =
    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(LongNumber)))

  private def paramToken(pp: PathParameter[_]): String =
    s"{${pp.name.name}}"

  private def toPm0[T](pm: PathMatcher[T]): PathMatcher0 =
    pm.tmap(_ => ())

  private def toDir0[T](d: Directive[T]): Directive0 =
    d.tmap(_ => ())

  implicit def bodyParamConverter[T: FromRequestUnmarshaller]: ConvertibleToDirective0[BodyParameter[T]] =
    instance(_ => entity(as[T]).tmap(_ => ()))

  implicit val hNilConverter: ConvertibleToDirective0[HNil] = instance(_ => pass)

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective0[H],
                                             tail: ConvertibleToDirective0[T]):
  ConvertibleToDirective0[H :: T] = {
    instance(
      l => head.convertToDirective0(l.head) & tail.convertToDirective0(l.tail),
      l => head.paramMap(l.head) ++ tail.paramMap(l.tail)
    )
  }

}