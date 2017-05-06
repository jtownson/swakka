package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import net.jtownson.swakka.OpenApiModel.{BodyParameter, PathParameter, QueryParameter}
import shapeless.HList


trait ConvertibleToDirective[T] {
  def convertToDirective(t: T): Directive1[T]

  def paramMap(t: T): Map[String, PathMatcher0]
}

object ConvertibleToDirective {

  import shapeless.{::, HNil}

  private def instance[T](f: T => Directive1[T]): ConvertibleToDirective[T] =
    instance(f, _ => Map())

  private def instance[T](f: T => Directive1[T], g: T => Map[String, PathMatcher0]): ConvertibleToDirective[T] =
    new ConvertibleToDirective[T] {
      def convertToDirective(t: T): Directive1[T] = f(t)
      def paramMap(t: T): Map[String, PathMatcher0] = g(t)
    }

  private val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""^(?i)(true|false)$""".r) flatMap (s => Some(s.toBoolean))

  private val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string ⇒
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }


  implicit val stringQueryConverter: ConvertibleToDirective[QueryParameter[String]] = new ConvertibleToDirective[QueryParameter[String]] {
    override def convertToDirective(qp: QueryParameter[String]) = {
      val f: (String) => QueryParameter[String] = (s: String) => {
        val copy: QueryParameter[String] = qp.copy()
        copy.value = s
        copy
      }

      parameter(qp.name).map(f)
    }

    override def paramMap(t: QueryParameter[String]) = Map()
  }
//    instance(qp => toDir0(parameter(qp.name)))

  implicit val floatQueryConverter: ConvertibleToDirective[QueryParameter[Float]] = ???
//    instance(qp => toDir0(parameter(qp.name.as[Float])))

  implicit val doubleQueryConverter: ConvertibleToDirective[QueryParameter[Double]] = ???
//    instance(qp => toDir0(parameter(qp.name.as[Double])))

  implicit val booleanQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] = ???
//    instance(qp => toDir0(parameter(qp.name.as[Boolean])))

  implicit val intQueryConverter: ConvertibleToDirective[QueryParameter[Int]] = ???
//    instance(qp => toDir0(parameter(qp.name.as[Int])))

  implicit val longQueryConverter: ConvertibleToDirective[QueryParameter[Long]] = ???
//    instance(qp => toDir0(parameter(qp.name.as[Long])))

  implicit val stringPathConverter: ConvertibleToDirective[PathParameter[String]] = ???
//    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(Segment)))

  implicit val floatPathConverter: ConvertibleToDirective[PathParameter[Float]] = ???
//    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(FloatNumber)))

  implicit val doublePathConverter: ConvertibleToDirective[PathParameter[Double]] = ???
//    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(DoubleNumber)))

  implicit val booleanPathConverter: ConvertibleToDirective[PathParameter[Boolean]] = ???
//    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(BooleanSegment)))

  implicit val intPathConverter: ConvertibleToDirective[PathParameter[Int]] = ???
//    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(IntNumber)))

  implicit val longPathConverter: ConvertibleToDirective[PathParameter[Long]] = ???
//    instance(pp => pass, pp => Map(paramToken(pp) -> toPm0(LongNumber)))

  private def paramToken(pp: PathParameter[_]): String =
    s"{${pp.name.name}}"

  private def toPm0[T](pm: PathMatcher[T]): PathMatcher0 =
    pm.tmap(_ => ())

  private def toDir0[T](d: Directive[T]): Directive0 =
    d.tmap(_ => ())

  implicit def bodyParamConverter[T: FromRequestUnmarshaller]: ConvertibleToDirective[BodyParameter[T]] = ???
//    instance(_ => entity(as[T]).tmap(_ => ()))

  implicit val hNilConverter: ConvertibleToDirective[HNil] = ???
    //instance(_ => pass)

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective[H],
                                             tail: ConvertibleToDirective[T]):
  ConvertibleToDirective[H :: T] = ???
//  {
//    instance(
//      l => head.convertToDirective0(l.head) & tail.convertToDirective0(l.tail),
//      l => head.paramMap(l.head) ++ tail.paramMap(l.tail)
//    )
//  }

}