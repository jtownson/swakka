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
import net.jtownson.swakka.model.Parameters.{BodyParameter, PathParameter, QueryParameter}
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

  case class Error(id: Int, message: String)

  type ListPetsParams = QueryParameter[Int] :: HNil
  type ListPetsResponses = ResponseValue[Pets, Header[String]] :: ResponseValue[Error, HNil] :: HNil

  type CreatePetParams = BodyParameter[Pet] :: HNil
  type CreatePetResponses = ResponseValue[HNil, HNil] :: ResponseValue[Error, HNil] :: HNil

  type ShowPetParams = PathParameter[String] :: HNil
  type ShowPetResponses = ResponseValue[Pets, HNil] :: ResponseValue[Error, HNil] :: HNil

  type Paths =
    PathItem[ListPetsParams, ListPetsResponses] ::
    PathItem[CreatePetParams, CreatePetResponses]::
    PathItem[ShowPetParams, ShowPetResponses]::
    HNil

  implicit val petSchemaWriter = schemaWriter(Pet)
  implicit val petJsonFormat = jsonFormat3(Pet)
  implicit val errorSchemaWriter = schemaWriter(Error)
  implicit val errorJsonFormat = jsonFormat2(Error)


  val petsDb = mutable.LinkedHashMap[String, Pet]()

  def listPets(params: ListPetsParams): Route =
    params match {
      case (QueryParameter(limit) :: HNil) => {
        val jsonResponse = petsDb.values.take(limit).toList.toJson
        complete(jsonResponse)
      }
    }

  def createPet(params: CreatePetParams): Route =
    params match {
      case (BodyParameter(pet) :: HNil) => {
        val petId = UUID.randomUUID().toString
        val newPet = pet.copy(id = petId)
        petsDb.put(petId, newPet)
        complete(StatusCodes.Created)
      }
    }

  def getPet(params: ShowPetParams): Route = params match {
    case (PathParameter(petId) :: HNil) =>
      val maybePet = petsDb.get(petId)
      maybePet match {
        case Some(pet) => complete(pet.toJson)
        case None => complete(NotFound)
      }
  }

  val petstoreApi = OpenApi[Paths](
    info = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(License(name = "MIT"))),
    host = Some("petstore.swagger.io:8080"),
    basePath = Some("/v1"),
    schemes = Some(Seq("http")),
    consumes = Some(Seq("application/json")),
    produces = Some(Seq("application/json")),
    paths =
      PathItem[ListPetsParams, ListPetsResponses](
        path = "/pets",
        method = GET,
        operation = Operation(
          summary = Some("List all pets"),
          operationId = Some("listPets"),
          tags = Some(Seq("pets")),
          parameters =
            QueryParameter[Int](
              name = 'limit,
              description = Some("How many items to return at one time (max 100)")) ::
              HNil,
          responses =
            ResponseValue[Pets, Header[String]](
              responseCode = "200",
              description = "An paged array of pets",
              headers = Header[String](Symbol("x-next"), Some("A link to the next page of responses"))) ::
              ResponseValue[Error, HNil](
                responseCode = "default",
                description = "unexpected error"
              ) :: HNil,
          endpointImplementation = listPets)) ::
        PathItem[CreatePetParams, CreatePetResponses](
          path = "/pets",
          method = POST,
          operation = Operation(
            summary = Some("Create a pet"),
            operationId = Some("createPets"),
            tags = Some(Seq("pets")),
            parameters = BodyParameter[Pet](name = 'pet, description = Some("the pet to create")) :: HNil,
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
            endpointImplementation = createPet
          )
        ) ::
        PathItem[ShowPetParams, ShowPetResponses](
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
              ResponseValue[Pets, HNil]("200", "Expected response to a valid request") ::
                ResponseValue[Error, HNil]("default", "unexpected error") ::
                HNil,
            endpointImplementation = getPet
          )
        ) ::
        HNil
  )

  val corsHeaders = List(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET, POST"))

  val apiRoutes = openApiRoute(
    api = petstoreApi,
    swaggerRouteSettings = Some(SwaggerRouteSettings(corsUseCase = SpecificallyThese(corsHeaders)))
  )

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val bindingFuture = Http().bindAndHandle(
    apiRoutes,
    "localhost",
    8080)
}