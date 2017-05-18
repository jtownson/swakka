package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import net.jtownson.swakka.model.Parameters.BodyParameter.OpenBodyParameter
import net.jtownson.swakka.model.Parameters.HeaderParameter.OpenHeaderParameter
import net.jtownson.swakka.model.Parameters.PathParameter.OpenPathParameter
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.model.Parameters.{BodyParameter, HeaderParameter, PathParameter, QueryParameter}
import net.jtownson.swakka.routegen.PathHandling.pathWithParamMatcher
import shapeless.{::, HList, HNil}

trait ConvertibleToDirective[T] {
  def convertToDirective(modelPath: String, t: T): Directive1[T]
}

object ConvertibleToDirective {

  val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""^(?i)(true|false)$""".r) flatMap (s => Some(s.toBoolean))

  val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string =>
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }

  def converter[T](t: T)(implicit ev: ConvertibleToDirective[T]): ConvertibleToDirective[T] = ev

  implicit val stringQueryConverter: ConvertibleToDirective[QueryParameter[String]] =
    instance(qp => parameter(qp.name).map(close(qp)))

  implicit val floatQueryConverter: ConvertibleToDirective[QueryParameter[Float]] =
    instance(qp => parameter(qp.name.as[Float]).map(close(qp)))

  implicit val doubleQueryConverter: ConvertibleToDirective[QueryParameter[Double]] =
    instance(qp => parameter(qp.name.as[Double]).map(close(qp)))

  implicit val booleanQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] =
    instance(qp => parameter(qp.name.as[Boolean]).map(close(qp)))

  implicit val intQueryConverter: ConvertibleToDirective[QueryParameter[Int]] =
    instance(qp => parameter(qp.name.as[Int]).map(close(qp)))

  implicit val longQueryConverter: ConvertibleToDirective[QueryParameter[Long]] =
    instance(qp => parameter(qp.name.as[Long]).map(close(qp)))

  implicit val stringPathConverter: ConvertibleToDirective[PathParameter[String]] =
    pathParamDirective(Segment)

  implicit val floatPathConverter: ConvertibleToDirective[PathParameter[Float]] =
    pathParamDirective(FloatNumber)

  implicit val doublePathConverter: ConvertibleToDirective[PathParameter[Double]] =
    pathParamDirective(DoubleNumber)

  implicit val booleanPathConverter: ConvertibleToDirective[PathParameter[Boolean]] =
    pathParamDirective(BooleanSegment)

  implicit val intPathConverter: ConvertibleToDirective[PathParameter[Int]] =
    pathParamDirective(IntNumber)

  implicit val longPathConverter: ConvertibleToDirective[PathParameter[Long]] =
    pathParamDirective(LongNumber)

  implicit val stringHeaderConverter: ConvertibleToDirective[HeaderParameter[String]] =
    headerParamDirective(s => s)

  implicit val floatHeaderConverter: ConvertibleToDirective[HeaderParameter[Float]] =
    headerParamDirective(_.toFloat)

  implicit val doubleHeaderConverter: ConvertibleToDirective[HeaderParameter[Double]] =
    headerParamDirective(_.toDouble)

  implicit val booleanHeaderConverter: ConvertibleToDirective[HeaderParameter[Boolean]] =
    headerParamDirective(_.toBoolean)

  implicit val intHeaderConverter: ConvertibleToDirective[HeaderParameter[Int]] =
    headerParamDirective(_.toInt)

  implicit val longHeaderConverter: ConvertibleToDirective[HeaderParameter[Long]] =
    headerParamDirective(_.toLong)

  implicit def bodyParamConverter[T: FromRequestUnmarshaller]: ConvertibleToDirective[BodyParameter[T]] =
    (_: String, bp: BodyParameter[T]) => entity(as[T]).map(close(bp))

  implicit val hNilConverter: ConvertibleToDirective[HNil] =
    (_: String, _: HNil) => pass.tmap[HNil](_ => shapeless.HNil)

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective[H],
                                             tail: ConvertibleToDirective[T]): ConvertibleToDirective[H :: T] =
    (modelPath: String, l: H :: T) => {
      val headDirective: Directive1[H] = head.convertToDirective(modelPath, l.head)
      val tailDirective: Directive1[T] = tail.convertToDirective(modelPath, l.tail)

      (headDirective & tailDirective).tmap((t: (H, T)) => t._1 :: t._2)
    }

  private def headerParamDirective[T](valueParser: String => T):
    ConvertibleToDirective[HeaderParameter[T]] =
    (_: String, hp: HeaderParameter[T]) => {

      val hMatcher: HttpHeader => Option[T] =
        httpHeader => if (httpHeader.is(hp.name.name.toLowerCase)) Some(valueParser(httpHeader.value())) else None

      headerValue(hMatcher).map(close(hp))
    }


  private def instance[T](f: T => Directive1[T]): ConvertibleToDirective[T] =
    (_: String, t: T) => f(t)

  private def close[T](qp: QueryParameter[T]): T => QueryParameter[T] =
    t => qp.asInstanceOf[OpenQueryParameter[T]].closeWith(t)

  private def close[T](pp: PathParameter[T]): T => PathParameter[T] =
    t => pp.asInstanceOf[OpenPathParameter[T]].closeWith(t)

  private def close[T](bp: BodyParameter[T]): T => BodyParameter[T] =
    t => bp.asInstanceOf[OpenBodyParameter[T]].closeWith(t)

  private def close[T](hp: HeaderParameter[T]): T => HeaderParameter[T] =
    t => hp.asInstanceOf[OpenHeaderParameter[T]].closeWith(t)

  private def pathParamDirective[T](pm: PathMatcher1[T]): ConvertibleToDirective[PathParameter[T]] = {
    (modelPath: String, pp: PathParameter[T]) =>
      rawPathPrefixTest(pathWithParamMatcher(modelPath, pp.name.name, pm)).map(close(pp))
  }

}
