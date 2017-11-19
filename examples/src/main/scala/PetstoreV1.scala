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

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import net.jtownson.swakka.OpenApiModel.{OpenApi, Operation, PathItem}
import net.jtownson.swakka.RouteGen.openApiRoute
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.model.Parameters.{
  BodyParameter,
  PathParameter,
  QueryParameter
}
import net.jtownson.swakka.model.Responses.{Header, ResponseValue}
import net.jtownson.swakka.model.{Info, License}
import net.jtownson.swakka.routegen.CorsUseCases.SpecificallyThese
import net.jtownson.swakka.routegen.SwaggerRouteSettings
import net.jtownson.swakka.OpenApiJsonProtocol._
import shapeless.{::, HNil}
import spray.json._

import scala.collection.mutable

object PetstoreV1 extends App {

  case class Pet(id: String, name: String, tag: Option[String] = None)

  type Pets = Seq[Pet]
  //  implicit val petSchemaWriter = schemaWriter(Pet)
  implicit val petJsonFormat = jsonFormat3(Pet)

  case class Error(id: Int, message: String)

  //  implicit val errorSchemaWriter = schemaWriter(Error)
  implicit val errorJsonFormat = jsonFormat2(Error)

  val petsDb = mutable.LinkedHashMap[String, Pet]()

  def listPets(limit: Int): Route = {
    val jsonResponse = petsDb.values.take(limit).toList.toJson
    complete(jsonResponse)
  }

  def createPet(pet: Pet): Route = {
    val petId = UUID.randomUUID().toString
    val newPet = pet.copy(id = petId)
    petsDb.put(petId, newPet)
    complete(StatusCodes.Created)
  }

  def getPet(petId: String): Route = {
    val maybePet = petsDb.get(petId)
    maybePet match {
      case Some(pet) => complete(pet.toJson)
      case None      => complete(NotFound)
    }
  }

  val petstoreApi = OpenApi(
    info = Info(version = "1.0.0",
                title = "Swagger Petstore",
                licence = Some(License(name = "MIT"))),
    host = Some("petstore.swagger.io:8080"),
    basePath = Some("/v1"),
    schemes = Some(Seq("http")),
    consumes = Some(Seq("application/json")),
    produces = Some(Seq("application/json")),
    paths =
      PathItem(
        path = "/pets",
        method = GET,
        operation = Operation(
          summary = Some("List all pets"),
          operationId = Some("listPets"),
          tags = Some(Seq("pets")),
          parameters =
            QueryParameter[Int](
              name = 'limit,
              description =
                Some("How many items to return at one time (max 100)")) ::
              HNil,
          responses =
            ResponseValue[Pets, Header[String]](
              responseCode = "200",
              description = "An paged array of pets",
              headers =
                Header[String](Symbol("x-next"),
                               Some("A link to the next page of responses"))) ::
              ResponseValue[Error, HNil](
              responseCode = "default",
              description = "unexpected error"
            ) :: HNil,
          endpointImplementation = listPets _
        )
      ) ::
        PathItem(
        path = "/pets",
        method = POST,
        operation = Operation(
          summary = Some("Create a pet"),
          operationId = Some("createPets"),
          tags = Some(Seq("pets")),
          parameters = BodyParameter[Pet](name = 'pet,
                                          description =
                                            Some("the pet to create")) :: HNil,
          responses =
            ResponseValue[HNil, HNil](
              responseCode = "201",
              description = "Null response"
            ) ::
              ResponseValue[Error, HNil](
              responseCode = "default",
              description = "unexpected error"
            ) ::
              HNil,
          endpointImplementation = createPet _
        )
      ) ::
        PathItem(
        path = "/pets/{petId}",
        method = GET,
        operation = Operation(
          summary = Some("Info for a specific pet"),
          operationId = Some("showPetById"),
          tags = Some(Seq("pets")),
          parameters =
            PathParameter[String]('petId, Some("The id of the pet to retrieve")) ::
              HNil,
          responses =
            ResponseValue[Pets, HNil]("200",
                                      "Expected response to a valid request") ::
              ResponseValue[Error, HNil]("default", "unexpected error") ::
              HNil,
          endpointImplementation = getPet _
        )
      ) ::
        HNil
  )

  val corsHeaders = List(RawHeader("Access-Control-Allow-Origin", "*"),
                         RawHeader("Access-Control-Allow-Methods", "GET, POST"))

  val apiRoutes = openApiRoute(
    api = petstoreApi,
    swaggerRouteSettings =
      Some(SwaggerRouteSettings(corsUseCase = SpecificallyThese(corsHeaders)))
  )

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val bindingFuture = Http().bindAndHandle(apiRoutes, "localhost", 8080)
}
