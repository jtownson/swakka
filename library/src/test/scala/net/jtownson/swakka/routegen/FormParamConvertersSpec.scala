package net.jtownson.swakka.routegen

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{MalformedFormFieldRejection, MissingFormFieldRejection, Route}
import net.jtownson.swakka.OpenApiJsonProtocol.{jsonFormat1, jsonFormat2, _}
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.model.Parameters.FormParameter
import net.jtownson.swakka.routegen.ConvertibleToDirective.{formParamConverter, _}
import org.scalatest.FlatSpec
import org.scalatest.Inside.inside
import org.scalatest.Matchers._
import spray.json._

class FormParamConvertersSpec extends FlatSpec with ConverterTest {

  import Tuplers._

  case class A(f: Int)

  it should "convert a single-field form" in {
    implicit val aJsonFormat = jsonFormat1(A)
    implicit val aSchemaWriter = schemaWriter(A)
    implicit val aFormConverter = formParamConverter(A)

    val a = A(1).toJson.compactPrint

    val formParameter = FormParameter(name = 'f, construct = A)

    val route: Route = aFormConverter.convertToDirective("", formParameter) {
      fp => complete(fp.value)
    }

    converterTest[A, FormParameter[(Int), A]](
      Post("http://example.com/p", FormData(Map("f" -> "1"))), a, route)
  }

  it should "convert a form with non-optional parameters" in {
    implicit val petFormat = jsonFormat2(Pet)
    implicit val petSchemaWriter = schemaWriter(Pet)
    implicit val formConverter = formParamConverter(Pet)

    val pet = Pet(1, "tiddles").toJson.compactPrint

    val formParameter = FormParameter[(Int, String), Pet](name = 'f, construct = Pet)

    val route: Route = formConverter.convertToDirective("", formParameter) {
      fp => complete(fp.value)
    }

    converterTest[Pet, FormParameter[(Int, String), Pet]](
      Post("http://example.com/p", FormData(Map("id" -> "1", "name" -> "tiddles"))),
      pet, route)
  }

  case class StrayPet(id: Int, name: Option[String])

  it should "convert a form with optional parameters" in {

    implicit val petFormat = jsonFormat2(StrayPet)
    implicit val petSchemaWriter = schemaWriter(StrayPet)
    implicit val formConverter = formParamConverter(StrayPet)

    val pet = StrayPet(1, None).toJson.compactPrint

    val formParameter = FormParameter(name = 'f, construct = StrayPet)

    val route: Route = formConverter.convertToDirective("", formParameter) {
      fp => complete(fp.value)
    }

    converterTest[StrayPet, FormParameter[(Int, String), StrayPet]](
      Post("http://example.com/p", FormData("id" -> "1")),
      pet, route)
  }

  it should "reject a form with missing mandatory parameters" in {

    implicit val petFormat = jsonFormat2(StrayPet)
    implicit val petSchemaWriter = schemaWriter(StrayPet)
    implicit val formConverter = formParamConverter(StrayPet)

    val formParameter = FormParameter(name = 'f, construct = StrayPet)

    val route: Route = formConverter.convertToDirective("", formParameter) {
      fp => complete(fp.value)
    }

    Post("http://example.com/p", FormData()) ~> route ~> check {
      rejection shouldBe MissingFormFieldRejection("id")
    }
  }

  it should "reject a form with parameters of unmarshallable type" in {

    implicit val petFormat = jsonFormat2(StrayPet)
    implicit val petSchemaWriter = schemaWriter(StrayPet)
    implicit val formConverter = formParamConverter(StrayPet)

    val formParameter = FormParameter(name = 'f, construct = StrayPet)

    val route: Route = formConverter.convertToDirective("", formParameter) {
      fp => complete(fp.value)
    }

    Post("http://example.com/p", FormData("id" -> "not-an-int")) ~> route ~> check {
      inside(rejection) {
        case MalformedFormFieldRejection(fieldName, _, Some(cause)) =>
          fieldName shouldBe "id"
          cause shouldBe a[NumberFormatException]
        case x => println(x)
      }
    }
  }
}
