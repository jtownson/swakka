/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jtownson.swakka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import io.swagger.annotations.ApiModelProperty
import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.model.HttpMethods.{DELETE, GET, POST, PUT}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FileInfo
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import spray.json._
import net.jtownson.swakka.jsonprotocol._
import net.jtownson.swakka.routegen._
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.syntax.singleton._
import shapeless.{HNil, ::}

class Petstore2Spec
    extends FlatSpec
    with RouteTest
    with TestFrameworkInterface {

  case class Order(id: Option[Long],
                   petId: Option[Long],
                   quantity: Option[Int],
                   status: Option[String],
                   complete: Option[Boolean],
                   shipDate: Option[String])

  implicit val orderJsonFormat: RootJsonFormat[Order] = jsonFormat6(Order)

  val dummyRoute: Route = complete("dummy")

  val storeOrder: () => Route = () => dummyRoute

  val operation: Operation[HNil, () => Route, ResponseValue[Order, HNil]] = Operation(
    tags = Some(Seq("store")),
    summary = Some("Place an order for a pet"),
    description = Some(""),
    operationId = Some("placeOrder"),
    produces = Some(Seq("application/xml", "application/json")),
    parameters = HNil: HNil,
    responses =
      ResponseValue[Order, HNil](
        responseCode = "200",
        description = "successful operation"
      ),
    endpointImplementation = storeOrder
  )

  type P = PathItem[HNil, () => Route, ResponseValue[Order, HNil]] :: HNil

  val paths: P = PathItem(
    path = "/store/order",
    method = POST,
    operation = operation
  ) :: HNil

  "Swakka" should "support the petstore v2 example" in {

//    val i1 = implicitly[RouteGen[P]]
//    val i2 = implicitly[JsonFormat[OpenApi[P, Nothing]]]

    val petstoreApi: OpenApi[P, Nothing] = OpenApi(paths = paths)

    val apiRoutes = openApiRoute(petstoreApi, Some(SwaggerRouteSettings()))()

    Get("http://petstore.swagger.io/v2/swagger.json") ~> apiRoutes ~> check {
      JsonParser(responseAs[String]) shouldBe "foo"
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
