package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives.{DoubleNumber, rawPathPrefixTest}
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1}
import net.jtownson.swakka.model.Parameters.PathParameter
import net.jtownson.swakka.model.Parameters.PathParameter.OpenPathParameter
import net.jtownson.swakka.routegen.PathHandling.pathWithParamMatcher
import akka.http.scaladsl.server.PathMatchers.{IntNumber, LongNumber, Segment}

trait PathParamConverters {

  val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""^(?i)(true|false)$""".r) flatMap (s => Some(s.toBoolean))

  val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string =>
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }

  implicit val stringReqPathConverter: ConvertibleToDirective[PathParameter[String]] =
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

  private def close[T](pp: PathParameter[T]): T => PathParameter[T] =
    t => pp.asInstanceOf[OpenPathParameter[T]].closeWith(t)

  private def pathParamDirective[T](pm: PathMatcher1[T]): ConvertibleToDirective[PathParameter[T]] = {
    (modelPath: String, pp: PathParameter[T]) =>
      rawPathPrefixTest(pathWithParamMatcher(modelPath, pp.name.name, pm)).map(close(pp))
  }
}
