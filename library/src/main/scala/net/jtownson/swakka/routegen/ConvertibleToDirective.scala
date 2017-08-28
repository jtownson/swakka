package net.jtownson.swakka.routegen

import akka.http.scaladsl.server._

trait ConvertibleToDirective[T] {
  def convertToDirective(modelPath: String, t: T): Directive1[T]
}

object ConvertibleToDirective
  extends BodyParamConverters
    with FormParamConverters
    with HeaderParamConverters
    with HListParamConverters
    with PathParamConverters
    with QueryParamConverters {

  def converter[T](t: T)(implicit ev: ConvertibleToDirective[T]): ConvertibleToDirective[T] = ev

}
