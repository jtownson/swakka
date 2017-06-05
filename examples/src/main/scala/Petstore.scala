import java.util.UUID

import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.complete
import net.jtownson.swakka.OpenApiModel.{OpenApi, Operation, PathItem}
import net.jtownson.swakka.RouteGen.openApiRoute
import net.jtownson.swakka.jsonschema.SchemaWriter.schemaWriter
import net.jtownson.swakka.model.{Info, Licence}
import net.jtownson.swakka.model.Parameters.{PathParameter, QueryParameter}
import net.jtownson.swakka.model.Responses.{Header, ResponseValue}
import net.jtownson.swakka.routegen.SwaggerRouteSettings
import shapeless.{::, HNil}
import spray.json._

import scala.collection.immutable.Seq
import scala.collection.mutable

object Petstore extends App {

  case class Pet(
                  id: Long,
                  name: String,
                  tag: Option[String] = None)

  type Pets = Seq[Pet]

  case class Error(
                    id: Int,
                    message: String
                  )

  implicit val petSchemaWriter = schemaWriter(Pet)
  implicit val errorSchemaWriter = schemaWriter(Error)

  type ListPetsParams = QueryParameter[Int] :: HNil
  type ListPetsResponses = ResponseValue[Pets, Header[String]] :: ResponseValue[Error, HNil] :: HNil

  type CreatePetParams = HNil
  type CreatePetResponses = ResponseValue[HNil, HNil] :: ResponseValue[Error, HNil] :: HNil

  type ShowPetParams = PathParameter[String] :: HNil
  type ShowPetResponses = ResponseValue[Pets, HNil] :: ResponseValue[Error, HNil] :: HNil

  type Paths = PathItem[ListPetsParams, ListPetsResponses] :: PathItem[HNil, CreatePetResponses] :: PathItem[ShowPetParams, ShowPetResponses] :: HNil

  val petsDb = mutable.LinkedHashMap[String, Pet]()

  def listPets(params: ListPetsParams): Route = {
    params match {
      case (limitParameter :: HNil) => {
        val jsonResponse = petsDb.values.take(limitParameter.value).toJson.prettyPrint
        complete(
          HttpResponse(
            status = OK,
            headers = Seq(),
            entity = jsonResponse))
      }
    }
  }

  def createPet(params: CreatePetParams): Route = {
    val newPet = Pet(petsDb.size+1, )
    petsDb.put(UUID.randomUUID().toString, )
  }

  def getPet(params: ShowPetParams): Route = {
    ???
  }

  val petstoreApi = OpenApi[Paths](
    info = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(Licence(name = "MIT"))),
    host = Some("petstore.swagger.io"),
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
            parameters = HNil,
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

  val apiRoutes = openApiRoute(petstoreApi, Some(SwaggerRouteSettings()))

}
