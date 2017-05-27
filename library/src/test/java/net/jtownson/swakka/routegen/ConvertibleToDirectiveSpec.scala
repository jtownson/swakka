package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.model.Parameters._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.HNil
import spray.json._
import net.jtownson.swakka.OpenApiJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter.schemaWriter
import net.jtownson.swakka.routegen.ConvertibleToDirective._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class ConvertibleToDirectiveSpec extends FlatSpec with RouteTest with TestFrameworkInterface {

  "ConvertibleToDirective" should "convert a string query parameter" in {
    converterTest[String, QueryParameter[String]](get("/path?q=x"), "x", QueryParameter[String]('q))
    converterTest[Option[String], QueryParameter[Option[String]]](get("/path?q=x"), "Some(x)", QueryParameter[Option[String]]('q))
    converterTest[Option[String], QueryParameter[Option[String]]](get("/path"), "None", QueryParameter[Option[String]]('q))
  }

  it should "convert a float query parameter" in {
    converterTest[Float, QueryParameter[Float]](get("/path?q=3.14"), "3.14", QueryParameter[Float]('q))
    converterTest[Option[Float], QueryParameter[Option[Float]]](get("/path?q=3.14"), "Some(3.14)", QueryParameter[Option[Float]]('q))
    converterTest[Option[Float], QueryParameter[Option[Float]]](get("/path"), "None", QueryParameter[Option[Float]]('q))
  }

  it should "convert a double query parameter" in {
    converterTest[Double, QueryParameter[Double]](get("/path?q=3.14"), "3.14", QueryParameter[Double]('q))
    converterTest[Option[Double], QueryParameter[Option[Double]]](get("/path?q=3.14"), "Some(3.14)", QueryParameter[Option[Double]]('q))
    converterTest[Option[Double], QueryParameter[Option[Double]]](get("/path"), "None", QueryParameter[Option[Double]]('q))
  }

  it should "convert a boolean query parameter" in {
    converterTest[Boolean, QueryParameter[Boolean]](get("/path?q=true"), "true", QueryParameter[Boolean]('q))
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](get("/path?q=true"), "Some(true)", QueryParameter[Option[Boolean]]('q))
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](get("/path"), "None", QueryParameter[Option[Boolean]]('q))
  }

  it should "convert an int query parameter" in {
    converterTest[Int, QueryParameter[Int]](get("/path?q=2"), "2", QueryParameter[Int]('q))
    converterTest[Option[Int], QueryParameter[Option[Int]]](get("/path?q=2"), "Some(2)", QueryParameter[Option[Int]]('q))
    converterTest[Option[Int], QueryParameter[Option[Int]]](get("/path"), "None", QueryParameter[Option[Int]]('q))
  }

  it should "convert a long query parameter" in {
    converterTest[Long, QueryParameter[Long]](get("/path?q=2"), "2", QueryParameter[Long]('q))
    converterTest[Option[Long], QueryParameter[Option[Long]]](get("/path?q=2"), "Some(2)", QueryParameter[Option[Long]]('q))
    converterTest[Option[Long], QueryParameter[Option[Long]]](get("/path"), "None", QueryParameter[Option[Long]]('q))
  }

  it should "convert a string header parameter" in {
    converterTest[String, HeaderParameter[String]](get("/", "x-p", "x"), "x", HeaderParameter[String](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/", "x-p", "x"), "Some(x)", HeaderParameter[Option[String]](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/"), "None", HeaderParameter[Option[String]](Symbol("x-p")))
  }

  it should "convert a float header parameter" in {
    converterTest[Float, HeaderParameter[Float]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Float](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Float]](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/"), "None", HeaderParameter[Option[Float]](Symbol("x-p")))
  }

  it should "convert a double header parameter" in {
    converterTest[Double, HeaderParameter[Double]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Double](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Double]](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/"), "None", HeaderParameter[Option[Double]](Symbol("x-p")))
  }

  it should "convert a boolean header parameter" in {
    converterTest[Boolean, HeaderParameter[Boolean]](get("/", "x-p", "true"), "true", HeaderParameter[Boolean](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/", "x-p", "true"), "Some(true)", HeaderParameter[Option[Boolean]](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/"), "None", HeaderParameter[Option[Boolean]](Symbol("x-p")))
  }

  it should "convert a int header parameter" in {
    converterTest[Int, HeaderParameter[Int]](get("/", "x-p", "2"), "2", HeaderParameter[Int](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Int]](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/"), "None", HeaderParameter[Option[Int]](Symbol("x-p")))
  }

  it should "convert a long header parameter" in {
    converterTest[Long, HeaderParameter[Long]](get("/", "x-p", "2"), "2", HeaderParameter[Long](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Long]](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/"), "None", HeaderParameter[Option[Long]](Symbol("x-p")))
  }

  // NB: according to the open api schema def, path parameters must have required = true.
  // i.e. Option types not supported.
  it should "convert a string path parameter" in {
    converterTest[String, PathParameter[String]](get("/a/x"), "x", PathParameter[String]('p), "/a/{p}")
  }

  it should "convert a float path parameter" in {
    converterTest[Float, PathParameter[Float]](get("/a/3.14"), "3.14", PathParameter[Float]('p), "/a/{p}")
  }

  it should "convert a double path parameter" in {
    converterTest[Double, PathParameter[Double]](get("/a/3.14"), "3.14", PathParameter[Double]('p), "/a/{p}")
  }

  it should "convert a boolean path parameter" in {
    converterTest[Boolean, PathParameter[Boolean]](get("/a/true"), "true", PathParameter[Boolean]('p), "/a/{p}")
  }

  it should "convert an int path parameter" in {
    converterTest[Int, PathParameter[Int]](get("/a/2"), "2", PathParameter[Int]('p), "/a/{p}")
  }

  it should "convert a long path parameter" in {
    converterTest[Long, PathParameter[Long]](get("/a/2"), "2", PathParameter[Long]('p), "/a/{p}")
  }


  it should "convert a body param of String" in {
    converterTest[String, BodyParameter[String]](post("/p", "Hello"), "Hello", BodyParameter[String]('p))
  }

  case class Pet(id: Int, name: String)

  it should "convert a required body param of a case class" in {
    implicit val petFormat = jsonFormat2(Pet)
    implicit val petSchemaWriter = schemaWriter(Pet)
    implicit val ev: ConvertibleToDirective[BodyParameter[Pet]] = bodyParamConverter[Pet]

    val pet = Pet(1, "tiddles").toJson.compactPrint

    val route: Route = bodyParamConverter[Pet].convertToDirective("", BodyParameter[Pet]('p)) { bp =>
      complete(bp.value)
    }
    converterTest[Pet, BodyParameter[Pet]](post("/p", pet), pet, route)
  }

  it should "convert an optional body param of a case class" in {
    implicit val petFormat = jsonFormat2(Pet)
    implicit val petSchemaWriter = schemaWriter(Pet)
    implicit val ev: ConvertibleToDirective[BodyParameter[Option[Pet]]] = bodyParamConverter[Option[Pet]]

    val route: Route = bodyOptParamConverter[Pet].convertToDirective("", BodyParameter[Option[Pet]]('p)) { bp =>
      bp.value match {
        case Some(pet) => fail("should have got nothing")
        case None => complete("got nothing")
      }
    }

    converterTest[Option[Pet], BodyParameter[Option[Pet]]](post("/p"), "got nothing", route)
  }

  "HNil converter" should "match paths without parameter tokens" in {

    val toDirective: Directive1[HNil] = hNilConverter.convertToDirective("/a/b", HNil)

    val route = toDirective { _ =>
      complete("matched")
    }

    Get("http://localhost/a/b") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "matched"
    }

    Get("http://localhost/x/y") ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }

  it should "pass paths with parameter tokens" in {

    val toDirective: Directive1[HNil] = hNilConverter.convertToDirective("/a/{b}", HNil)

    val route = toDirective { _ =>
      complete("matched")
    }

    Get("http://localhost/x/y") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "matched"
    }
  }

  private def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, expectedResponse: String, param: U, modelPath: String = "")
                              (implicit ev: ConvertibleToDirective[U]): Unit = {
    converterTest(request, expectedResponse, route[T, U](modelPath, param))
  }

  private def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, expectedResponse: String, route: Route): Unit = {
    request ~> route ~> check {
      responseAs[String] shouldBe expectedResponse
    }
  }

  private def route[T, U <: Parameter[T]](modelPath: String, param: U)
                      (implicit ev: ConvertibleToDirective[U]): Route = {
    converter(param)(ev).convertToDirective(modelPath, param) {
      qpc => complete(qpc.value.toString)
    }
  }

  private def get(path: String, header: String, value: String): HttpRequest =
    get(path).withHeaders(List(RawHeader(header, value)))

  private def post(path: String, body: String): HttpRequest =
    Post(s"http://example.com$path", HttpEntity(ContentTypes.`application/json`, body))

  private def post(path: String): HttpRequest =
    Post(s"http://example.com$path")

  private def get(path: String): HttpRequest =
    Get(s"http://example.com$path")

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
