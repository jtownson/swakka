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

  case class Pet(id: Long, name: String, tag: Option[String] = None)

  type Pets = Seq[Pet]

  case class Error(
      id: Int,
      message: String
  )

  case class ApiResponse(
      code: Option[Int],
      `type`: Option[String],
      message: Option[String]
  )

//  implicit object OrderStatus extends Enumeration {
//    type OrderStatus = Value
//    val placed, approved, delivered = Value
//  }
//
//  import OrderStatus._
  case class Order(
                  id: Long,
                  petId: Long,
                  quantity: Int,
//                  shipDate: DateTime,
//                  status: OrderStatus,
                  complete: Boolean/* = false*/
                  )

  implicit val petJsonFormat = jsonFormat3(Pet)

  implicit val errorJsonFormat = jsonFormat2(Error)

  implicit val apiResponseJsonFormat = jsonFormat3(ApiResponse)

//  implicit val orderStatusSchemaWriter = enumSchemaWriter(OrderStatus)
//  implicit val orderStatusJsonFormat = new EnumJsonConverter(OrderStatus)
  implicit val orderJsonFormat = jsonFormat4(Order)

  type Params = BodyParameter[Order] :: HNil
  type Responses = ResponseValue[Order, HNil] :: ResponseValue[HNil, HNil] :: HNil
  type Endpoint = Order => Route

  val dummyRoute: Route = complete("dummy")

  val createPet: Pet => Route = _ => dummyRoute

  val updatePet: Pet => Route = _ => dummyRoute

  val findByStatus: Seq[String] => Route = _ => dummyRoute

  val findByTags: Seq[String] => Route = _ => dummyRoute

  val findById: Long => Route = _ => dummyRoute

  val updatePetForm: (Long, Option[String], Option[String]) => Route =
    (_, _, _) => dummyRoute

  val deletePet: (Option[String], Long) => Route = (_, _) => dummyRoute

  val uploadImage: (Long,
    Option[String],
    Option[(FileInfo, Source[ByteString, Any])]) => Route =
    (_, _, _) => dummyRoute

  val storeOrder: Order => Route = _ => dummyRoute

  "Swakka" should "support the petstore v2 example" in {

    val securityDefinitions =
      'petstore_auth ->> Oauth2ImplicitSecurity(
        authorizationUrl = "http://petstore.swagger.io/oauth/dialog",
        scopes = Some(
          Map("write:pets" -> "modify pets in your account",
              "read:pets" -> "read your pets"))
      ) ::
        'api_key ->> ApiKeyInHeaderSecurity("api_key") ::
        HNil

    val petstoreApi = OpenApi(
      info = Info(
        description = Some(
          "This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters."),
        version = "1.0.0",
        title = "Swagger Petstore",
        licence =
          Some(
            License(name = "Apache 2.0",
                    url =
                      Some("http://www.apache.org/licenses/LICENSE-2.0.html"))),
        termsOfService = Some("http://swagger.io/terms/")
      ),
      tags = Some(
        Seq(
          Tag(name = "pet",
              description = Some("Everything about your Pets"),
              externalDocs = Some(
                ExternalDocs(url = "http://swagger.io",
                             description = Some("Find out more")))),
          Tag(name = "store", description = Some("Access to Petstore orders")),
          Tag(
            name = "user",
            description = Some("Operations about users"),
            externalDocs = Some(
              ExternalDocs(url = "http://swagger.io",
                           description = Some("Find out more about our store")))
          )
        )),
      host = Some("petstore.swagger.io"),
      basePath = Some("/v2"),
      schemes = Some(Seq("http")),
      paths =
        PathItem(
          path = "/pets",
          method = POST,
          operation = Operation(
            summary = Some("Add a new pet to the store"),
            description = Some(""),
            operationId = Some("addPet"),
            tags = Some(Seq("pets")),
            consumes = Some(Seq("application/json", "application/xml")),
            produces = Some(Seq("application/xml", "application/json")),
            parameters = BodyParameter[Pet](
              'body,
              Some("Pet object that needs to be added to the store")) :: HNil,
            responses =
              ResponseValue[HNil, HNil](
                responseCode = "201",
                description = "Pet added to the store"
              ) ::
                ResponseValue[HNil, HNil](
                responseCode = "405",
                description = "Invalid input"
              ) ::
                ResponseValue[Error, HNil](
                responseCode = "default",
                description = "unexpected error"
              ) ::
                HNil,
            security = Some(
              Seq(SecurityRequirement('petstore_auth,
                                      Seq("write:pets", "read:pets")))),
            endpointImplementation = createPet
          )
        ) ::
          PathItem(
          path = "/pets",
          method = PUT,
          operation = Operation(
            summary = Some("Update an existing pet"),
            description = Some(""),
            operationId = Some("updatePet"),
            tags = Some(Seq("pet")),
            consumes = Some(Seq("application/json", "application/xml")),
            produces = Some(Seq("application/xml", "application/json")),
            parameters = BodyParameter[Pet](
              'body,
              Some("Pet object that needs to be added to the store")) :: HNil,
            responses =
              ResponseValue[HNil, HNil](
                responseCode = "400",
                description = "Invalid ID supplied"
              ) ::
                ResponseValue[HNil, HNil](
                responseCode = "404",
                description = "Pet not found"
              ) ::
                ResponseValue[HNil, HNil](
                responseCode = "405",
                description = "Validation exception"
              ) ::
                HNil,
            security = Some(
              Seq(SecurityRequirement('petstore_auth,
                                      Seq("write:pets", "read:pets")))),
            endpointImplementation = updatePet
          )
        ) ::
          PathItem(
          path = "/pet/findByStatus",
          method = GET,
          operation = Operation(
            summary = Some("Finds Pets by status"),
            description = Some(
              "Multiple status values can be provided with comma separated strings"),
            operationId = Some("findPetsByStatus"),
            tags = Some(Seq("pet")),
            produces = Some(Seq("application/xml", "application/json")),
            parameters = MultiValued[String, QueryParameter[String]](
              QueryParameter[String](
                name = 'status,
                description =
                  Some("Status values that need to be considered for filter"),
                default = Some("available"),
                enum = Some(Seq("available", "pending", "sold"))
              )) :: HNil,
            responses =
              ResponseValue[Seq[Pet], HNil]("200", "successful operation") ::
                ResponseValue[HNil, HNil]("400", "Invalid status value") ::
                HNil,
            endpointImplementation = findByStatus,
            security =
              Some(Seq(SecurityRequirement('petstore_auth,
                                           Seq("write:pets", "read:pets"))))
          )
        ) ::
          PathItem(
          path = "/pet/findByTags",
          method = GET,
          operation = Operation(
            deprecated = true,
            summary = Some("Finds Pets by tags"),
            tags = Some(Seq("pet")),
            description = Some(
              "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing."),
            operationId = Some("findPetsByTags"),
            produces = Some(Seq("application/xml", "application/json")),
            parameters = MultiValued[String, QueryParameter[String]](
              QueryParameter[String](
                name = 'tags,
                description = Some("Tags to filter by"))) :: HNil,
            responses = ResponseValue[Seq[Pet], HNil]("200",
                                                      "successful operation") ::
              ResponseValue[HNil, HNil]("400", "Invalid tag value") ::
              HNil,
            endpointImplementation = findByTags,
            security =
              Some(Seq(SecurityRequirement('petstore_auth,
                                           Seq("write:pets", "read:pets"))))
          )
        ) ::
          PathItem(
          path = "/pet/{petId}",
          method = GET,
          operation = Operation(
            summary = Some("Find pet by ID"),
            description = Some("Returns a single pet"),
            operationId = Some("getPetById"),
            tags = Some(Seq("pet")),
            produces = Some(Seq("application/xml", "application/json")),
            parameters =
              PathParameter[Long](name = 'petId,
                                  description = Some("ID of pet to return"))
                :: HNil,
            responses =
              ResponseValue[Pet, HNil](responseCode = "200",
                                       description = "successful operation") ::
                ResponseValue[HNil, HNil](
                responseCode = "400",
                description = "Invalid ID supplied"
              ) ::
                ResponseValue[HNil, HNil](responseCode = "404",
                                          description = "Pet not found") ::
                HNil,
            endpointImplementation = findById,
            security = Some(Seq(SecurityRequirement('api_key, Seq())))
          )
        ) ::
          PathItem(
          path = "/pet/{petId}",
          method = POST,
          operation = Operation(
            tags = Some(Seq("pet")),
            summary = Some("Updates a pet in the store with form data"),
            description = Some(""),
            operationId = Some("updatePetWithForm"),
            consumes = Some(Seq("application/x-www-form-urlencoded")),
            produces = Some(Seq("application/xml", "application/json")),
            parameters =
              PathParameter[Long](
                name = 'petId,
                description = Some("ID of pet that needs to be updated")
              ) ::
                FormFieldParameter[Option[String]](
                name = 'name,
                description = Some("Updated name of the pet")
              ) ::
                FormFieldParameter[Option[String]](
                name = 'status,
                description = Some("Updated status of the pet")
              ) ::
                HNil,
            responses = ResponseValue[HNil, HNil](
              responseCode = "405",
              description = "Invalid input"
            ),
            security =
              Some(Seq(SecurityRequirement('petstore_auth,
                                           Seq("write:pets", "read:pets")))),
            endpointImplementation = updatePetForm
          )
        ) ::
          PathItem(
          path = "/pet/{petId}",
          method = DELETE,
          operation = Operation(
            tags = Some(Seq("pet")),
            summary = Some("Deletes a pet"),
            description = Some(""),
            operationId = Some("deletePet"),
            produces = Some(Seq("application/xml", "application/json")),
            parameters =
              HeaderParameter[Option[String]](name = 'api_key) ::
                PathParameter[Long](
                name = 'petId,
                description = Some("Pet id to delete")
              ) ::
                HNil,
            responses =
              ResponseValue[HNil, HNil](
                responseCode = "400",
                description = "Invalid ID supplied"
              ) ::
                ResponseValue[HNil, HNil](
                responseCode = "404",
                description = "Pet not found"
              ) ::
                HNil,
            security =
              Some(Seq(SecurityRequirement('petstore_auth,
                                           Seq("write:pets", "read:pets")))),
            endpointImplementation = deletePet
          )
        ) ::
          PathItem(
          path = "/pet/{petId}/uploadImage",
          method = POST,
          operation = Operation(
            tags = Some(Seq("pet")),
            summary = Some("uploads an image"),
            description = Some(""),
            operationId = Some("uploadFile"),
            consumes = Some(Seq("multipart/form-data")),
            produces = Some(Seq("application/json")),
            parameters =
              PathParameter[Long](
                name = 'petId,
                description = Some("ID of pet to update")
              ) ::
                FormFieldParameter[Option[String]](
                name = 'additionalMetadata,
                description = Some("Additional data to pass to server")
              ) ::
                FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]](
                name = 'file,
                description = Some("file to upload")
              ) ::
                HNil,
            responses =
              ResponseValue[ApiResponse, HNil](
                responseCode = "200",
                description = "successful operation",
              ) ::
                HNil,
            security =
              Some(Seq(SecurityRequirement('petstore_auth,
                                           Seq("write:pets", "read:pets")))),
            endpointImplementation = uploadImage
          )
        ) ::
          PathItem(
            path = "/store/inventory",
            method = GET,
            operation = Operation(
              tags = Some(Seq("store")),
              summary = Some("Returns pet inventories by status"),
              description = Some("Returns a map of status codes to quantities"),
              operationId = Some("getInventory"),
              produces = Some(Seq("application/json")),
              responses =
                ResponseValue[Map[Int, String], HNil](
                  responseCode = "200",
                  description = "successful operation"
                ) ::
                HNil,
              security = Some(Seq(SecurityRequirement('api_key))),
              endpointImplementation = () => dummyRoute
            )
          )
//          ::
//          PathItem(
//            path = "/store/order",
//            method = POST,
//            operation = Operation(
//              tags = Some(Seq("store")),
//              summary = Some("Place an order for a pet"),
//              description = Some(""),
//              operationId = Some("placeOrder"),
//              produces = Some(Seq("application/xml", "application/json")),
//              parameters = HNil,
//                BodyParameter[Order](
//                  name = 'body,
//                  description = Some("order placed for purchasing the pet")
//                ) ::
//                HNil,
//              responses = HNil,
//                ResponseValue[Order, HNil](
//                  responseCode = "200",
//                  description = "successful operation"
//                ) ::
//                ResponseValue[HNil, HNil](
//                  responseCode = "400",
//                  description = "Invalid Order"
//                ) ::
//                HNil,
//              endpointImplementation = () => dummyRoute //storeOrder
//            )
//          )
          :: HNil,
      securityDefinitions = Some(securityDefinitions)
    )

    val apiRoutes = openApiRoute(petstoreApi, Some(SwaggerRouteSettings()))

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "description" -> JsString(
          "This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters."),
        "title" -> JsString("Swagger Petstore"),
        "version" -> JsString("1.0.0"),
        "license" -> JsObject(
          "name" -> JsString("Apache 2.0"),
          "url" -> JsString("http://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        "termsOfService" -> JsString("http://swagger.io/terms/")
      ),
      "host" -> JsString("petstore.swagger.io"),
      "basePath" -> JsString("/v2"),
      "schemes" -> JsArray(JsString("http")),
      "paths" -> JsObject(
        "/pets" -> JsObject(
          "post" -> JsObject(
            "summary" -> JsString("Add a new pet to the store"),
            "description" -> JsString(""),
            "operationId" -> JsString("addPet"),
            "tags" -> JsArray(JsString("pets")),
            "consumes" -> JsArray(JsString("application/json"),
                                  JsString("application/xml")),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "parameters" -> JsArray(
              JsObject(
                "in" -> JsString("body"),
                "name" -> JsString("body"),
                "description" -> JsString(
                  "Pet object that needs to be added to the store"),
                "required" -> JsBoolean(true),
                "schema" -> JsObject(
                  "type" -> JsString("object"),
                  "required" -> JsArray(JsString("id"), JsString("name")),
                  "properties" -> JsObject(
                    "id" -> JsObject("type" -> JsString("integer"),
                                     "format" -> JsString("int64")),
                    "name" -> JsObject("type" -> JsString("string")),
                    "tag" -> JsObject("type" -> JsString("string"))
                  )
                )
              )
            ),
            "responses" -> JsObject(
              "201" -> JsObject(
                "description" -> JsString("Pet added to the store")
              ),
              "405" -> JsObject(
                "description" -> JsString("Invalid input")
              ),
              "default" -> JsObject(
                "description" -> JsString("unexpected error"),
                "schema" -> JsObject(
                  "type" -> JsString("object"),
                  "required" -> JsArray(JsString("id"), JsString("message")),
                  "properties" -> JsObject(
                    "id" -> JsObject(
                      "type" -> JsString("integer"),
                      "format" -> JsString("int32")
                    ),
                    "message" -> JsObject(
                      "type" -> JsString("string")
                    )
                  )
                )
              )
            ),
            "security" -> JsArray(
              JsObject(
                "petstore_auth" -> JsArray(JsString("write:pets"),
                                           JsString("read:pets"))
              )
            )
          ),
          "put" -> JsObject(
            "tags" -> JsArray(JsString("pet")),
            "summary" -> JsString("Update an existing pet"),
            "description" -> JsString(""),
            "operationId" -> JsString("updatePet"),
            "consumes" -> JsArray(JsString("application/json"),
                                  JsString("application/xml")),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "parameters" -> JsArray(
              JsObject(
                "in" -> JsString("body"),
                "name" -> JsString("body"),
                "description" -> JsString(
                  "Pet object that needs to be added to the store"),
                "required" -> JsBoolean(true),
                "schema" -> JsObject(
                  "type" -> JsString("object"),
                  "required" -> JsArray(JsString("id"), JsString("name")),
                  "properties" -> JsObject(
                    "id" -> JsObject("type" -> JsString("integer"),
                                     "format" -> JsString("int64")),
                    "name" -> JsObject("type" -> JsString("string")),
                    "tag" -> JsObject("type" -> JsString("string"))
                  )
                )
              )
            ),
            "responses" -> JsObject(
              "400" -> JsObject(
                "description" -> JsString("Invalid ID supplied")
              ),
              "404" -> JsObject(
                "description" -> JsString("Pet not found")
              ),
              "405" -> JsObject(
                "description" -> JsString("Validation exception")
              )
            ),
            "security" -> JsArray(
              JsObject(
                "petstore_auth" -> JsArray(JsString("write:pets"),
                                           JsString("read:pets"))
              )
            )
          )
        ),
        "/pet/findByStatus" -> JsObject(
          "get" -> JsObject(
            "tags" -> JsArray(JsString("pet")),
            "summary" -> JsString("Finds Pets by status"),
            "description" -> JsString(
              "Multiple status values can be provided with comma separated strings"),
            "operationId" -> JsString("findPetsByStatus"),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("status"),
                "in" -> JsString("query"),
                "description" -> JsString(
                  "Status values that need to be considered for filter"),
                "required" -> JsBoolean(true),
                "type" -> JsString("array"),
                "items" -> JsObject(
                  "type" -> JsString("string"),
                  "enum" -> JsArray(
                    JsString("available"),
                    JsString("pending"),
                    JsString("sold")
                  )
                  // The petstorev2 sample has that required = true but there is also a default value.
                  // This seems to make no sense because if the user always provides a value, the default is redundant.
                  //                  ,
                  //                  "default" -> JsString("available")
                ),
                "collectionFormat" -> JsString("multi")
              )
            ),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("successful operation"),
                "schema" -> JsObject(
                  "type" -> JsString("array"),
                  "items" -> JsObject(
                    "type" -> JsString("object"),
                    "required" -> JsArray(JsString("id"), JsString("name")),
                    "properties" -> JsObject(
                      "id" -> JsObject("type" -> JsString("integer"),
                                       "format" -> JsString("int64")),
                      "name" -> JsObject("type" -> JsString("string")),
                      "tag" -> JsObject("type" -> JsString("string"))
                    )
                  )
                )
              ),
              "400" -> JsObject(
                "description" -> JsString("Invalid status value")
              )
            ),
            "security" -> JsArray(
              JsObject(
                "petstore_auth" -> JsArray(JsString("write:pets"),
                                           JsString("read:pets"))
              )
            )
          )
        ),
        "/pet/findByTags" -> JsObject(
          "get" -> JsObject(
            "tags" -> JsArray(JsString("pet")),
            "summary" -> JsString("Finds Pets by tags"),
            "description" -> JsString(
              "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing."),
            "operationId" -> JsString("findPetsByTags"),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("tags"),
                "in" -> JsString("query"),
                "description" -> JsString("Tags to filter by"),
                "required" -> JsBoolean(true),
                "type" -> JsString("array"),
                "items" -> JsObject(
                  "type" -> JsString("string")
                ),
                "collectionFormat" -> JsString("multi")
              )
            ),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("successful operation"),
                "schema" -> JsObject(
                  "type" -> JsString("array"),
                  "items" -> JsObject(
                    "type" -> JsString("object"),
                    "required" -> JsArray(JsString("id"), JsString("name")),
                    "properties" -> JsObject(
                      "id" -> JsObject("type" -> JsString("integer"),
                                       "format" -> JsString("int64")),
                      "name" -> JsObject("type" -> JsString("string")),
                      "tag" -> JsObject("type" -> JsString("string"))
                    )
                  )
                )
              ),
              "400" -> JsObject(
                "description" -> JsString("Invalid tag value")
              )
            ),
            "security" -> JsArray(
              JsObject(
                "petstore_auth" -> JsArray(JsString("write:pets"),
                                           JsString("read:pets"))
              )
            ),
            "deprecated" -> JsBoolean(true)
          )
        ),
        "/pet/{petId}" -> JsObject(
          "get" -> JsObject(
            "security" -> JsArray(
              JsObject(
                "api_key" -> JsArray()
              )
            ),
            "description" -> JsString("Returns a single pet"),
            "tags" -> JsArray(JsString("pet")),
            "operationId" -> JsString("getPetById"),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "parameters" -> JsArray(
              JsObject(
                "format" -> JsString("int64"),
                "name" -> JsString("petId"),
                "in" -> JsString("path"),
                "description" -> JsString("ID of pet to return"),
                "type" -> JsString("integer"),
                "required" -> JsBoolean(true)
              )
            ),
            "summary" -> JsString("Find pet by ID"),
            "responses" ->
              JsObject(
                "200" ->
                  JsObject(
                    "description" -> JsString("successful operation"),
                    "schema" -> JsObject(
                      "type" -> JsString("object"),
                      "required" -> JsArray(JsString("id"), JsString("name")),
                      "properties" -> JsObject(
                        "id" -> JsObject("type" -> JsString("integer"),
                                         "format" -> JsString("int64")),
                        "name" -> JsObject("type" -> JsString("string")),
                        "tag" -> JsObject("type" -> JsString("string"))
                      )
                    )
                  ),
                "400" ->
                  JsObject(
                    "description" -> JsString("Invalid ID supplied")
                  ),
                "404" ->
                  JsObject(
                    "description" -> JsString("Pet not found")
                  )
              )
          ),
          "post" -> JsObject(
            "security" -> JsArray(
              JsObject(
                "petstore_auth" -> JsArray(JsString("write:pets"),
                                           JsString("read:pets"))
              )
            ),
            "description" -> JsString(""),
            "tags" -> JsArray(JsString("pet")),
            "operationId" -> JsString("updatePetWithForm"),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "consumes" -> JsArray(
              JsString("application/x-www-form-urlencoded")),
            "parameters" -> JsArray(
              JsObject(
                "format" -> JsString("int64"),
                "name" -> JsString("petId"),
                "in" -> JsString("path"),
                "description" -> JsString("ID of pet that needs to be updated"),
                "type" -> JsString("integer"),
                "required" -> JsBoolean(true)
              ),
              JsObject(
                "name" -> JsString("name"),
                "in" -> JsString("formData"),
                "description" -> JsString("Updated name of the pet"),
                "type" -> JsString("string"),
                "required" -> JsBoolean(false)
              ),
              JsObject(
                "name" -> JsString("status"),
                "in" -> JsString("formData"),
                "description" -> JsString("Updated status of the pet"),
                "type" -> JsString("string"),
                "required" -> JsBoolean(false)
              )
            ),
            "summary" -> JsString("Updates a pet in the store with form data"),
            "responses" ->
              JsObject(
                "405" ->
                  JsObject(
                    "description" -> JsString("Invalid input")
                  )
              )
          ),
          "delete" -> JsObject(
            "security" -> JsArray(
              JsObject(
                "petstore_auth" -> JsArray(JsString("write:pets"),
                                           JsString("read:pets"))
              )
            ),
            "description" -> JsString(""),
            "tags" -> JsArray(JsString("pet")),
            "operationId" -> JsString("deletePet"),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("api_key"),
                "in" -> JsString("header"),
                "required" -> JsBoolean(false),
                "type" -> JsString("string")
              ),
              JsObject(
                "format" -> JsString("int64"),
                "name" -> JsString("petId"),
                "in" -> JsString("path"),
                "description" -> JsString("Pet id to delete"),
                "type" -> JsString("integer"),
                "required" -> JsBoolean(true)
              )
            ),
            "summary" -> JsString("Deletes a pet"),
            "responses" ->
              JsObject(
                "400" ->
                  JsObject(
                    "description" -> JsString("Invalid ID supplied")
                  ),
                "404" ->
                  JsObject(
                    "description" -> JsString("Pet not found")
                  )
              )
          )
        ),
        "/pet/{petId}/uploadImage" -> JsObject(
          "post" ->
            JsObject(
              "security" -> JsArray(
                JsObject(
                  "petstore_auth" -> JsArray(JsString("write:pets"),
                                             JsString("read:pets"))
                )
              ),
              "description" -> JsString(""),
              "tags" -> JsArray(JsString("pet")),
              "operationId" -> JsString("uploadFile"),
              "produces" -> JsArray(JsString("application/json")),
              "consumes" -> JsArray(JsString("multipart/form-data")),
              "parameters" -> JsArray(
                JsObject(
                  "format" -> JsString("int64"),
                  "name" -> JsString("petId"),
                  "in" -> JsString("path"),
                  "description" -> JsString("ID of pet to update"),
                  "type" -> JsString("integer"),
                  "required" -> JsBoolean(true)
                ),
                JsObject(
                  "name" -> JsString("additionalMetadata"),
                  "in" -> JsString("formData"),
                  "description" -> JsString(
                    "Additional data to pass to server"),
                  "type" -> JsString("string"),
                  "required" -> JsBoolean(false)
                ),
                JsObject(
                  "name" -> JsString("file"),
                  "in" -> JsString("formData"),
                  "description" -> JsString("file to upload"),
                  "type" -> JsString("file"),
                  "required" -> JsBoolean(false)
                )
              ),
              "summary" -> JsString("uploads an image"),
              "responses" ->
                JsObject(
                  "200" ->
                    JsObject(
                      "description" -> JsString("successful operation"),
                      "schema" ->
                        JsObject(
                          "type" -> JsString("object"),
                          "properties" -> JsObject(
                            "code" -> JsObject(
                              "type" -> JsString("integer"),
                              "format" -> JsString("int32")
                            ),
                            "type" -> JsObject(
                              "type" -> JsString("string")
                            ),
                            "message" -> JsObject(
                              "type" -> JsString("string")
                            )
                          )
                        )
                    )
                )
            )
        ),
        "/store/inventory" ->
          JsObject(
            "get" ->
              JsObject(
                "security" -> JsArray(
                  JsObject(
                    "api_key" -> JsArray()
                  )
                ),
                "description" -> JsString(
                  "Returns a map of status codes to quantities"),
                "tags" -> JsArray(JsString("store")),
                "operationId" -> JsString("getInventory"),
                "produces" -> JsArray(JsString("application/json")),
                "parameters" -> JsArray(),
                "summary" -> JsString("Returns pet inventories by status"),
                "responses" ->
                  JsObject(
                    "200" ->
                      JsObject(
                        "description" -> JsString("successful operation"),
                        "schema" ->
                          JsObject(
                            "type" -> JsString("object"),
                            "additionalProperties" ->
                              JsObject(
                                "type" -> JsString("integer"),
                                "format" -> JsString("int32")
                              )
                          )
                      )
                  )
              )
          ),
        "/store/order" ->
          JsObject(
            "post" ->
              JsObject(
                "description" -> JsString(""),
                "tags" -> JsArray(JsString("store")),
                "operationId" -> JsString("placeOrder"),
                "produces" -> JsArray(JsString("application/xml"),
                                      JsString("application/json")),
                "parameters" -> JsArray(
                  JsObject(
                    "name" -> JsString("body"),
                    "in" -> JsString("body"),
                    "description" -> JsString(
                      "order placed for purchasing the pet"),
                    "schema" -> orderSchema,
                    "required" -> JsBoolean(true)
                  )
                ),
                "summary" -> JsString("Place an order for a pet"),
                "responses" ->
                  JsObject(
                    "200" ->
                      JsObject(
                        "description" -> JsString("successful operation"),
                        "schema" -> orderSchema
                      ),
                    "400" ->
                      JsObject(
                        "description" -> JsString("Invalid Order")
                      )
                  )
              )
          )
      )
    )

    Get("http://petstore.swagger.io/v2/swagger.json") ~> apiRoutes ~> check {
      JsonParser(responseAs[String]) shouldBe expectedJson
    }
  }

  private def orderSchema: JsObject =
    JsObject(
      "type" -> JsString("object"),
      "properties" ->
        JsObject(
          "shipDate" ->
            JsObject(
              "type" -> JsString("string"),
              "format" -> JsString("date-time")
            ),
          "quantity" ->
            JsObject(
              "type" -> JsString("integer"),
              "format" -> JsString("int32")
            ),
          "petId" ->
            JsObject(
              "type" -> JsString("integer"),
              "format" -> JsString("int64")
            ),
          "id" ->
            JsObject(
              "type" -> JsString("integer"),
              "format" -> JsString("int64")
            ),
          "complete" ->
            JsObject(
              "type" -> JsString("boolean"),
              "default" -> JsBoolean(false)
            ),
          "status" ->
            JsObject(
              "type" -> JsString("string"),
              "description" -> JsString("Order Status"),
              "enum" -> JsArray(JsString("placed"),
                                JsString("approved"),
                                JsString("delivered"))
            )
        )
    )

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
