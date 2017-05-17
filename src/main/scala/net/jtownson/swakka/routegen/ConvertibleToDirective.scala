package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import net.jtownson.swakka.OpenApiModel.{BodyParameter, PathParameter, QueryParameter}
import net.jtownson.swakka.routegen.PathHandling.pathWithParamMatcher
import shapeless.HList

trait ConvertibleToDirective[T] {
  def convertToDirective(modelPath: String, t: T): Directive1[T]
}

object ConvertibleToDirective {

  import shapeless.{::, HNil}

  private def instance[T](f: T => Directive1[T]): ConvertibleToDirective[T] =
    (modelPath: String, t: T) => f(t)

  val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""^(?i)(true|false)$""".r) flatMap (s => Some(s.toBoolean))

  private val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string ⇒
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }

  implicit val stringQueryConverter: ConvertibleToDirective[QueryParameter[String]] =
    instance(qp => parameter(qp.name).map(s => qp))

  implicit val floatQueryConverter: ConvertibleToDirective[QueryParameter[Float]] =
    instance(qp => parameter(qp.name.as[Float]).map(f => qp))

  implicit val doubleQueryConverter: ConvertibleToDirective[QueryParameter[Double]] =
    instance(qp => parameter(qp.name.as[Double]).map(d => qp))

  implicit val booleanQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] =
    instance(qp => parameter(qp.name.as[Boolean]).map(b => qp))

  implicit val intQueryConverter: ConvertibleToDirective[QueryParameter[Int]] =
    instance(qp => parameter(qp.name.as[Int]).map(i => qp))

  implicit val longQueryConverter: ConvertibleToDirective[QueryParameter[Long]] =
    instance(qp => parameter(qp.name.as[Long]).map(l => qp))

  private def pathParamDirective[T](pm: PathMatcher1[T]): ConvertibleToDirective[PathParameter[T]] = {
    (modelPath: String, pp: PathParameter[T]) => rawPathPrefixTest(pathWithParamMatcher(modelPath, pp.name.name, pm)).map(s => pp)
  }

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

  private def paramToken(pp: PathParameter[_]): String =
    s"{${pp.name.name}}"

  private def toPm0[T](pm: PathMatcher[T]): PathMatcher0 =
    pm.tmap(_ => ())

  private def toDir0[T](d: Directive[T]): Directive0 =
    d.tmap(_ => ())

  implicit def bodyParamConverter[T: FromRequestUnmarshaller]: ConvertibleToDirective[BodyParameter[T]] =
    (_: String, t: BodyParameter[T]) => entity(as[T]).tmap(_ => t)

  implicit val hNilConverter: ConvertibleToDirective[HNil] =
    (_: String, _: HNil) => pass.tmap[HNil](_ => shapeless.HNil)

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective[H],
                                             tail: ConvertibleToDirective[T]): ConvertibleToDirective[H :: T] =
    (modelPath: String, l: H :: T) => {
      val headDirective: Directive1[H] = head.convertToDirective(modelPath, l.head)
      val tailDirective: Directive1[T] = tail.convertToDirective(modelPath, l.tail)

      (headDirective & tailDirective).tmap((t: (H, T)) => t._1 :: t._2)
    }
}