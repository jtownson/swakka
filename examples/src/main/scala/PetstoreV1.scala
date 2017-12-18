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
import spray.json._

import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapiroutegen._
import net.jtownson.swakka.openapijson._

import scala.collection.mutable

object PetstoreV1 extends App {

  case class Pet(id: String, name: String, tag: Option[String] = None)

  type Pets = Seq[Pet]
  implicit val petJsonFormat = jsonFormat3(Pet)

  case class Error(id: Int, message: String)

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
      (
        PathItem(
        path = "/pets",
        method = GET,
        operation = Operation(
          summary = Some("List all pets"),
          operationId = Some("listPets"),
          tags = Some(Seq("pets")),
          parameters =
            Tuple1(QueryParameter[Int](
              name = 'limit,
              description =
                Some("How many items to return at one time (max 100)"))),
          responses =
            (ResponseValue[Pets, Header[String]](
              responseCode = "200",
              description = "An paged array of pets",
              headers =
                Header[String](Symbol("x-next"),
                               Some("A link to the next page of responses"))),
              ResponseValue[Error](
                responseCode = "default",
                description = "unexpected error"
            )),
          endpointImplementation = listPets _
        )
      ),
        PathItem(
        path = "/pets",
        method = POST,
        operation = Operation(
          summary = Some("Create a pet"),
          operationId = Some("createPets"),
          tags = Some(Seq("pets")),
          parameters = Tuple1(BodyParameter[Pet](name = 'pet,
                                          description =
                                            Some("the pet to create"))),
          responses =
            (ResponseValue[Unit](
              responseCode = "201",
              description = "Null response"
            ),
            ResponseValue[Error](
              responseCode = "default",
              description = "unexpected error"
            )),
          endpointImplementation = createPet _
        )
      ),
        PathItem(
        path = "/pets/{petId}",
        method = GET,
        operation = Operation(
          summary = Some("Info for a specific pet"),
          operationId = Some("showPetById"),
          tags = Some(Seq("pets")),
          parameters =
            Tuple1(PathParameter[String]('petId, Some("The id of the pet to retrieve"))),
          responses =
            (ResponseValue[Pets]("200",
                                      "Expected response to a valid request"),
              ResponseValue[Error]("default", "unexpected error")),
          endpointImplementation = getPet _
        )
      ))
  )

  val corsHeaders = List(RawHeader("Access-Control-Allow-Origin", "*"),
                         RawHeader("Access-Control-Allow-Methods", "GET, POST"))

  val apiRoutes = openApiRoute(
    api = petstoreApi,
    swaggerRouteSettings =
      Some(DocRouteSettings(corsUseCase = SpecificallyThese(corsHeaders)))
  )

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val bindingFuture = Http().bindAndHandle(apiRoutes, "localhost", 8080)
}
