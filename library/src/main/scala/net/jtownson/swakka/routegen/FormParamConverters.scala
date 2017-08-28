package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FormFieldDirectives.FieldMagnet._
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, _}

import akka.http.scaladsl.server.Directives.{formField, provide}
import akka.http.scaladsl.server.{Directive1, MissingFormFieldRejection}
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
import net.jtownson.swakka.jsonschema.ApiModelDictionary.apiModelKeys
import net.jtownson.swakka.model.Parameters.FormParameter
import net.jtownson.swakka.model.Parameters.FormParameter.OpenFormParameter
import scala.reflect.runtime.universe.TypeTag

trait FormParamConverters {

  sealed trait FormFieldDefaults[T] {
    def getOrDefault(fieldName: String, t: Option[T]): Directive1[T]
  }

  implicit def mandatoryFormFieldDefaults[T]: FormFieldDefaults[T] = new FormFieldDefaults[T] {
    override def getOrDefault(fieldName: String, t: Option[T]): Directive1[T] =
      if (t.isDefined)
        provide(t.get)
      else
        reject(MissingFormFieldRejection(fieldName))
  }

  implicit def optionalFormFieldDefaults[U]: FormFieldDefaults[Option[U]] = new FormFieldDefaults[Option[U]] {
    override def getOrDefault(fieldName: String, t: Option[Option[U]]): Directive1[Option[U]] =
      provide(t.flatten)
  }

  def formParamConverter[P : FromStringUnmarshaller, T: TypeTag]
  (constructor: P => T)
  (implicit p1Default: FormFieldDefaults[P]): ConvertibleToDirective[FormParameter[P, T]] = {

    val fields: Seq[String] = apiModelKeys[T]

    (_: String, fp: FormParameter[P, T]) => {
      fieldDirective[P](fields(0)).map((p: P) => closeSingle(fp)(constructor.apply(p)))
    }
  }

  def formParamConverter[P1: FromStringUnmarshaller, P2: FromStringUnmarshaller, T: TypeTag]
  (constructor: (P1, P2) => T)
  (implicit p1Default: FormFieldDefaults[P1], p2Default: FormFieldDefaults[P2]): ConvertibleToDirective[FormParameter[(P1, P2), T]] = {

    val fields: Seq[String] = apiModelKeys[T]

    (_: String, fp: FormParameter[(P1, P2), T]) => {
      (fieldDirective[P1](fields(0)) & fieldDirective[P2](fields(1))).tmap(t => close(fp)(constructor.tupled(t)))
    }
  }

  private def fieldDirective[T: FromStringUnmarshaller](fieldName: String)(implicit defaults: FormFieldDefaults[T]): Directive1[T] = {
    formField(fieldName.as[T].?).flatMap(ot => defaults.getOrDefault(fieldName, ot))
  }

  private def close[P <: Product, T](fp: FormParameter[P, T]): T => FormParameter[P, T] =
    t => fp.asInstanceOf[OpenFormParameter[P, T]].closeWith(t)

  private def closeSingle[P, T](fp: FormParameter[P, T]): T => FormParameter[P, T] =
    t => fp.asInstanceOf[OpenFormParameter[P, T]].closeWith(t)


}
