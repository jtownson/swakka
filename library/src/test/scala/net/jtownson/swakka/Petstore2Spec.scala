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

import io.swagger.annotations.ApiModelProperty
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
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.openapijson._
import net.jtownson.swakka.openapiroutegen._
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

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

  implicit object OrderStatus extends Enumeration {
    type OrderStatus = Value
    val placed, approved, delivered = Value
  }

  import OrderStatus._

  case class Order(
      id: Option[Long],
      petId: Option[Long],
      quantity: Option[Int],
      @ApiModelProperty("Order Status") status: Option[OrderStatus],
      shipDate: Option[DateTime],
      complete: Option[Boolean])

  case class User(id: Option[Long],
                  username: Option[String],
                  firstName: Option[String],
                  lastName: Option[String],
                  email: Option[String],
                  password: Option[String],
                  phone: Option[String],
                  @ApiModelProperty("User Status") userStatus: Option[Int])

  implicit val petJsonFormat: RootJsonFormat[Pet] = jsonFormat3(Pet)

  implicit val errorJsonFormat: RootJsonFormat[Error] = jsonFormat2(Error)

  implicit val apiResponseJsonFormat: RootJsonFormat[ApiResponse] = jsonFormat3(
    ApiResponse)

  implicit val orderStatusJsonFormat = new EnumJsonConverter(OrderStatus)
  implicit val orderJsonFormat: RootJsonFormat[Order] = jsonFormat6(Order)
  // This is an example of customizing the writing of JsonSchema for a class.
  implicit val orderSchemaWriter: SchemaWriter[Order] =
    (_: JsonSchema[Order]) => orderSchema

  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat8(User)

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

  val findOrderById: Long => Route = _ => dummyRoute
  val deleteOrderById: Long => Route = _ => dummyRoute

  val emptyEndpoint: () => Route = () => dummyRoute

  val createUser: User => Route = _ => dummyRoute

  val createUserArray: Seq[User] => Route = _ => dummyRoute

  val loginUser: (String, String) => Route = (_, _) => dummyRoute

  val logoutUser: () => Route = () => dummyRoute

  val userByName: String => Route = _ => dummyRoute

  val putUserByName: (String, User) => Route = (_, _) => dummyRoute

  val deleteUserByName: String => Route = _ => dummyRoute

  "Swakka" should "support the petstore v2 example" in {

    val securityDefinitions = (Oauth2ImplicitSecurity(
                                 key = "petstore_auth",
                                 authorizationUrl =
                                   "http://petstore.swagger.io/oauth/dialog",
                                 scopes = Some(Map(
                                   "write:pets" -> "modify pets in your account",
                                   "read:pets" -> "read your pets"))
                               ),
                               ApiKeyInHeaderSecurity("api_key"))

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
      paths = (PathItem(
                 path = "/pets",
                 method = POST,
                 operation = Operation(
                   summary = Some("Add a new pet to the store"),
                   description = Some(""),
                   operationId = Some("addPet"),
                   tags = Some(Seq("pets")),
                   consumes = Some(Seq("application/json", "application/xml")),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     BodyParameter[Pet](
                       'body,
                       Some("Pet object that needs to be added to the store"))),
                   responses = (ResponseValue[Unit](
                                  responseCode = "201",
                                  description = "Pet added to the store"
                                ),
                                ResponseValue[Unit](
                                  responseCode = "405",
                                  description = "Invalid input"
                                ),
                                ResponseValue[Error](
                                  responseCode = "default",
                                  description = "unexpected error"
                                )),
                   security = Some(
                     Seq(SecurityRequirement('petstore_auth,
                                             Seq("write:pets", "read:pets")))),
                   endpointImplementation = createPet
                 )
               ),
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
                   parameters = Tuple1(
                     BodyParameter[Pet](
                       'body,
                       Some("Pet object that needs to be added to the store"))),
                   responses = (ResponseValue[Unit](
                                  responseCode = "400",
                                  description = "Invalid ID supplied"
                                ),
                                ResponseValue[Unit](
                                  responseCode = "404",
                                  description = "Pet not found"
                                ),
                                ResponseValue[Unit](
                                  responseCode = "405",
                                  description = "Validation exception"
                                )),
                   security = Some(
                     Seq(SecurityRequirement('petstore_auth,
                                             Seq("write:pets", "read:pets")))),
                   endpointImplementation = updatePet
                 )
               ),
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
                   parameters = Tuple1(
                     MultiValued[String, QueryParameter[String]](
                       QueryParameter[String](
                         name = 'status,
                         description = Some(
                           "Status values that need to be considered for filter"),
                         default = Some("available"),
                         enum = Some(Seq("available", "pending", "sold"))
                       ))),
                   responses =
                     (ResponseValue[Seq[Pet]]("200", "successful operation"),
                      ResponseValue[Unit]("400", "Invalid status value")),
                   endpointImplementation = findByStatus,
                   security = Some(
                     Seq(SecurityRequirement('petstore_auth,
                                             Seq("write:pets", "read:pets"))))
                 )
               ),
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
                   parameters = Tuple1(
                     MultiValued[String, QueryParameter[String]](
                       QueryParameter[String](name = 'tags,
                                              description =
                                                Some("Tags to filter by")))),
                   responses =
                     (ResponseValue[Seq[Pet]]("200", "successful operation"),
                      ResponseValue[Unit]("400", "Invalid tag value")),
                   endpointImplementation = findByTags,
                   security = Some(
                     Seq(SecurityRequirement('petstore_auth,
                                             Seq("write:pets", "read:pets"))))
                 )
               ),
               PathItem(
                 path = "/pet/{petId}",
                 method = GET,
                 operation = Operation(
                   summary = Some("Find pet by ID"),
                   description = Some("Returns a single pet"),
                   operationId = Some("getPetById"),
                   tags = Some(Seq("pet")),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     PathParameter[Long](name = 'petId,
                                         description =
                                           Some("ID of pet to return"))),
                   responses =
                     (ResponseValue[Pet](responseCode = "200",
                                         description = "successful operation"),
                      ResponseValue[Unit](responseCode = "400",
                                          description = "Invalid ID supplied"),
                      ResponseValue[Unit](responseCode = "404",
                                          description = "Pet not found")),
                   endpointImplementation = findById,
                   security = Some(Seq(SecurityRequirement('api_key, Seq())))
                 )
               ),
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
                     (PathParameter[Long](
                        name = 'petId,
                        description = Some("ID of pet that needs to be updated")
                      ),
                      FormFieldParameter[Option[String]](
                        name = 'name,
                        description = Some("Updated name of the pet")
                      ),
                      FormFieldParameter[Option[String]](
                        name = 'status,
                        description = Some("Updated status of the pet")
                      )),
                   responses = ResponseValue[Unit](
                     responseCode = "405",
                     description = "Invalid input"
                   ),
                   security = Some(
                     Seq(SecurityRequirement('petstore_auth,
                                             Seq("write:pets", "read:pets")))),
                   endpointImplementation = updatePetForm
                 )
               ),
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
                     (HeaderParameter[Option[String]](name = 'api_key),
                      PathParameter[Long](
                        name = 'petId,
                        description = Some("Pet id to delete")
                      )),
                   responses = (ResponseValue[Unit](
                                  responseCode = "400",
                                  description = "Invalid ID supplied"
                                ),
                                ResponseValue[Unit](
                                  responseCode = "404",
                                  description = "Pet not found"
                                )),
                   security = Some(
                     Seq(SecurityRequirement('petstore_auth,
                                             Seq("write:pets", "read:pets")))),
                   endpointImplementation = deletePet
                 )
               ),
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
                     (PathParameter[Long](
                        name = 'petId,
                        description = Some("ID of pet to update")
                      ),
                      FormFieldParameter[Option[String]](
                        name = 'additionalMetadata,
                        description = Some("Additional data to pass to server")
                      ),
                      FormFieldParameter[Option[(FileInfo,
                                                 Source[ByteString, Any])]](
                        name = 'file,
                        description = Some("file to upload")
                      )),
                   responses = ResponseValue[ApiResponse](
                     responseCode = "200",
                     description = "successful operation"
                   ),
                   security = Some(
                     Seq(SecurityRequirement('petstore_auth,
                                             Seq("write:pets", "read:pets")))),
                   endpointImplementation = uploadImage
                 )
               ),
               PathItem(
                 path = "/store/inventory",
                 method = GET,
                 operation = Operation(
                   tags = Some(Seq("store")),
                   summary = Some("Returns pet inventories by status"),
                   description =
                     Some("Returns a map of status codes to quantities"),
                   operationId = Some("getInventory"),
                   produces = Some(Seq("application/json")),
                   responses = ResponseValue[Map[Int, String]](
                     responseCode = "200",
                     description = "successful operation"
                   ),
                   security = Some(Seq(SecurityRequirement('api_key))),
                   endpointImplementation = () => dummyRoute
                 )
               ),
               PathItem(
                 path = "/store/order",
                 method = POST,
                 operation = Operation(
                   tags = Some(Seq("store")),
                   summary = Some("Place an order for a pet"),
                   description = Some(""),
                   operationId = Some("placeOrder"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     BodyParameter[Order](
                       name = 'body,
                       description = Some("order placed for purchasing the pet")
                     )),
                   responses = (ResponseValue[Order](
                                  responseCode = "200",
                                  description = "successful operation"
                                ),
                                ResponseValue[Unit](
                                  responseCode = "400",
                                  description = "Invalid Order"
                                )),
                   endpointImplementation = storeOrder
                 )
               ),
               PathItem(
                 path = "/store/order/{orderId}",
                 method = GET,
                 operation = Operation(
                   tags = Some(Seq("store")),
                   summary = Some("Find purchase order by ID"),
                   description = Some(
                     "For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions"),
                   operationId = Some("getOrderById"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     PathParameterConstrained[Long, Long](
                       name = 'orderId,
                       description = Some("ID of pet that needs to be fetched"),
                       constraints =
                         Constraints(minimum = Some(1L), maximum = Some(10L))
                     )
                   ),
                   endpointImplementation = findOrderById,
                   responses = (
                     ResponseValue[Order]("200", "successful operation"),
                     ResponseValue[Unit]("400", "Invalid ID supplied"),
                     ResponseValue[Unit]("404", "Order not found")
                   )
                 )
               ),
               PathItem(
                 path = "/store/order/{orderId}",
                 method = DELETE,
                 operation = Operation(
                   tags = Some(Seq("store")),
                   summary = Some("Delete purchase order by ID"),
                   description = Some(
                     "For valid response try integer IDs with positive integer value. Negative or non-integer values will generate API errors"),
                   operationId = Some("deleteOrder"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     PathParameterConstrained[Long, Long](
                       name = 'orderId,
                       description =
                         Some("ID of the order that needs to be deleted"),
                       constraints = Constraints(minimum = Some(1L))
                     )
                   ),
                   endpointImplementation = deleteOrderById,
                   responses = (
                     ResponseValue[Unit]("400", "Invalid ID supplied"),
                     ResponseValue[Unit]("404", "Order not found")
                   )
                 )
               ),
               PathItem(
                 path = "/user",
                 method = POST,
                 operation = Operation(
                   tags = Some(Seq("user")),
                   summary = Some("Create user"),
                   description =
                     Some("This can only be done by the logged in user."),
                   operationId = Some("createUser"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     BodyParameter[User]('body,
                                         description =
                                           Some("Created user object"))
                   ),
                   endpointImplementation = createUser,
                   responses = ResponseValue[Unit](responseCode = "default",
                                                   description =
                                                     "successful operation")
                 )
               ),
               PathItem(
                 path = "/user/createWithArray",
                 method = POST,
                 operation = Operation(
                   tags = Some(Seq("user")),
                   summary =
                     Some("Creates list of users with given input array"),
                   description = Some(""),
                   operationId = Some("createUsersWithArrayInput"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     BodyParameter[Seq[User]](name = 'body,
                                              description =
                                                Some("List of user object"))
                   ),
                   endpointImplementation = createUserArray,
                   responses = ResponseValue[Unit](
                     responseCode = "default",
                     description = "successful operation"
                   )
                 )
               ),
               PathItem(
                 path = "/user/login",
                 method = GET,
                 operation = Operation(
                   tags = Some(Seq("user")),
                   summary = Some("Logs user into the system"),
                   description = Some(""),
                   operationId = Some("loginUser"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = (
                     QueryParameter[String](
                       name = 'username,
                       description = Some("The user name for login")
                     ),
                     QueryParameter[String](
                       name = 'password,
                       description =
                         Some("The password for login in clear text")
                     )
                   ),
                   endpointImplementation = loginUser,
                   responses = (
                     ResponseValue[String, (Header[Int], Header[DateTime])](
                       responseCode = "200",
                       description = "successful operation",
                       headers = (
                         Header[Int](
                           name = Symbol("X-Rate-Limit"),
                           description =
                             Some("calls per hour allowed by the user")
                         ),
                         Header[DateTime](
                           name = Symbol("X-Expires-After"),
                           description = Some("date in UTC when token expires")
                         )
                       )
                     ),
                     ResponseValue[Unit](
                       responseCode = "400",
                       description = "Invalid username/password supplied"
                     )
                   )
                 )
               ),
               PathItem(
                 path = "/user/logout",
                 method = GET,
                 operation = Operation(
                   tags = Some(Seq("user")),
                   summary = Some("Logs out current logged in user session"),
                   description = Some(""),
                   operationId = Some("logoutUser"),
                   produces = Some(Seq("application/xml", "application/json")),
                   endpointImplementation = logoutUser,
                   responses = ResponseValue[Unit](responseCode = "default",
                                                   description =
                                                     "successful operation")
                 )
               ),
               PathItem(
                 path = "/user/{username}",
                 method = GET,
                 operation = Operation(
                   tags = Some(Seq("user")),
                   summary = Some("Get user by user name"),
                   description = Some(""),
                   operationId = Some("getUserByName"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     PathParameter[String](
                       name = 'username,
                       description = Some(
                         "The name that needs to be fetched. Use user1 for testing. ")
                     )
                   ),
                   endpointImplementation = userByName,
                   responses = (
                     ResponseValue[User](
                       responseCode = "200",
                       description = "successful operation"
                     ),
                     ResponseValue[Unit](
                       responseCode = "400",
                       description = "Invalid username supplied"
                     ),
                     ResponseValue[Unit](
                       responseCode = "404",
                       description = "User not found"
                     )
                   )
                 )
               ),
               PathItem(
                 path = "/user/{username}",
                 method = PUT,
                 operation = Operation(
                   tags = Some(Seq("user")),
                   summary = Some("Updated user"),
                   description =
                     Some("This can only be done by the logged in user."),
                   operationId = Some("updateUser"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = (
                     PathParameter[String](
                       name = 'username,
                       description = Some("name that need to be updated")
                     ),
                     BodyParameter[User](
                       name = 'body,
                       description = Some("Updated user object")
                     )
                   ),
                   endpointImplementation = putUserByName,
                   responses = (
                     ResponseValue[Unit](
                       responseCode = "400",
                       description = "Invalid user supplied"
                     ),
                     ResponseValue[Unit](
                       responseCode = "404",
                       description = "User not found"
                     )
                   )
                 )
               ),
               PathItem(
                 path = "/user/{username}",
                 method = DELETE,
                 operation = Operation(
                   tags = Some(Seq("user")),
                   summary = Some("Delete user"),
                   description =
                     Some("This can only be done by the logged in user."),
                   operationId = Some("deleteUser"),
                   produces = Some(Seq("application/xml", "application/json")),
                   parameters = Tuple1(
                     PathParameter[String](
                       name = 'username,
                       description = Some("The name that needs to be deleted")
                     )
                   ),
                   endpointImplementation = deleteUserByName,
                   responses = (
                     ResponseValue[Unit](
                       responseCode = "400",
                       description = "Invalid username supplied"
                     ),
                     ResponseValue[Unit](
                       responseCode = "404",
                       description = "User not found"
                     )
                   )
                 )
               )),
      securityDefinitions = Some(securityDefinitions)
    )

    val apiRoutes = openApiRoute(petstoreApi, Some(DocRouteSettings()))

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
        "/store/inventory" -> JsObject(
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
        "/store/order" -> JsObject(
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
        ),
        "/store/order/{orderId}" -> JsObject(
          "get" ->
            JsObject(
              "description" -> JsString(
                "For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions"),
              "tags" -> JsArray(JsString("store")),
              "operationId" -> JsString("getOrderById"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "parameters" -> JsArray(
                JsObject(
                  "format" -> JsString("int64"),
                  "name" -> JsString("orderId"),
                  "in" -> JsString("path"),
                  "description" -> JsString(
                    "ID of pet that needs to be fetched"),
                  "maximum" -> JsNumber(10.0),
                  "minimum" -> JsNumber(1.0),
                  "type" -> JsString("integer"),
                  "required" -> JsBoolean(true)
                )
              ),
              "summary" -> JsString("Find purchase order by ID"),
              "responses" ->
                JsObject(
                  "200" ->
                    JsObject(
                      "description" -> JsString("successful operation"),
                      "schema" -> orderSchema
                    ),
                  "400" ->
                    JsObject(
                      "description" -> JsString("Invalid ID supplied")
                    ),
                  "404" ->
                    JsObject(
                      "description" -> JsString("Order not found")
                    )
                )
            ),
          "delete" ->
            JsObject(
              "description" -> JsString(
                "For valid response try integer IDs with positive integer value. Negative or non-integer values will generate API errors"),
              "tags" -> JsArray(JsString("store")),
              "operationId" -> JsString("deleteOrder"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "parameters" -> JsArray(
                JsObject(
                  "format" -> JsString("int64"),
                  "name" -> JsString("orderId"),
                  "in" -> JsString("path"),
                  "description" -> JsString(
                    "ID of the order that needs to be deleted"),
                  "minimum" -> JsNumber(1.0),
                  "type" -> JsString("integer"),
                  "required" -> JsBoolean(true)
                )
              ),
              "summary" -> JsString("Delete purchase order by ID"),
              "responses" ->
                JsObject(
                  "400" ->
                    JsObject(
                      "description" -> JsString("Invalid ID supplied")
                    ),
                  "404" ->
                    JsObject(
                      "description" -> JsString("Order not found")
                    )
                )
            )
        ),
        "/user" -> JsObject(
          "post" ->
            JsObject(
              "description" -> JsString(
                "This can only be done by the logged in user."),
              "tags" -> JsArray(JsString("user")),
              "operationId" -> JsString("createUser"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "parameters" -> JsArray(
                JsObject(
                  "name" -> JsString("body"),
                  "in" -> JsString("body"),
                  "description" -> JsString("Created user object"),
                  "schema" -> userSchema,
                  "required" -> JsBoolean(true)
                )
              ),
              "summary" -> JsString("Create user"),
              "responses" ->
                JsObject(
                  "default" ->
                    JsObject(
                      "description" -> JsString("successful operation")
                    )
                )
            )
        ),
        "/user/createWithArray" -> JsObject(
          "post" ->
            JsObject(
              "description" -> JsString(""),
              "tags" -> JsArray(JsString("user")),
              "operationId" -> JsString("createUsersWithArrayInput"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "parameters" -> JsArray(
                JsObject(
                  "name" -> JsString("body"),
                  "in" -> JsString("body"),
                  "description" -> JsString("List of user object"),
                  "schema" -> JsObject(
                    "type" -> JsString("array"),
                    "items" -> userSchema
                  ),
                  "required" -> JsBoolean(true)
                )
              ),
              "summary" -> JsString(
                "Creates list of users with given input array"),
              "responses" ->
                JsObject(
                  "default" ->
                    JsObject(
                      "description" -> JsString("successful operation")
                    )
                )
            )
        ),
        "/user/login" -> JsObject(
          "get" ->
            JsObject(
              "description" -> JsString(""),
              "tags" -> JsArray(JsString("user")),
              "operationId" -> JsString("loginUser"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "parameters" -> JsArray(
                JsObject(
                  "name" -> JsString("username"),
                  "in" -> JsString("query"),
                  "description" -> JsString("The user name for login"),
                  "type" -> JsString("string"),
                  "required" -> JsBoolean(true)
                ),
                JsObject(
                  "name" -> JsString("password"),
                  "in" -> JsString("query"),
                  "description" -> JsString(
                    "The password for login in clear text"),
                  "type" -> JsString("string"),
                  "required" -> JsBoolean(true)
                )
              ),
              "summary" -> JsString("Logs user into the system"),
              "responses" ->
                JsObject(
                  "200" ->
                    JsObject(
                      "description" -> JsString("successful operation"),
                      "schema" ->
                        JsObject(
                          "type" -> JsString("string")
                        ),
                      "headers" ->
                        JsObject(
                          "X-Rate-Limit" ->
                            JsObject(
                              "type" -> JsString("integer"),
                              "format" -> JsString("int32"),
                              "description" -> JsString(
                                "calls per hour allowed by the user")
                            ),
                          "X-Expires-After" ->
                            JsObject(
                              "type" -> JsString("string"),
                              "format" -> JsString("date-time"),
                              "description" -> JsString(
                                "date in UTC when token expires")
                            )
                        )
                    ),
                  "400" ->
                    JsObject(
                      "description" -> JsString(
                        "Invalid username/password supplied")
                    )
                )
            )
        ),
        "/user/logout" -> JsObject(
          "get" ->
            JsObject(
              "description" -> JsString(""),
              "tags" -> JsArray(JsString("user")),
              "operationId" -> JsString("logoutUser"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "summary" -> JsString("Logs out current logged in user session"),
              "responses" ->
                JsObject(
                  "default" ->
                    JsObject(
                      "description" -> JsString("successful operation")
                    )
                )
            )
        ),
        "/user/{username}" -> JsObject(
          "get" -> JsObject(
            "description" -> JsString(""),
            "tags" -> JsArray(JsString("user")),
            "operationId" -> JsString("getUserByName"),
            "produces" -> JsArray(JsString("application/xml"),
                                  JsString("application/json")),
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("username"),
                "in" -> JsString("path"),
                "description" -> JsString(
                  "The name that needs to be fetched. Use user1 for testing. "),
                "type" -> JsString("string"),
                "required" -> JsBoolean(true)
              )
            ),
            "summary" -> JsString("Get user by user name"),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("successful operation"),
                "schema" -> userSchema
              ),
              "400" -> JsObject(
                "description" -> JsString("Invalid username supplied")
              ),
              "404" -> JsObject(
                "description" -> JsString("User not found")
              )
            )
          ),
          "put" -> JsObject(
              "description" -> JsString(
                "This can only be done by the logged in user."),
              "tags" -> JsArray(JsString("user")),
              "operationId" -> JsString("updateUser"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "parameters" -> JsArray(
                JsObject(
                  "name" -> JsString("username"),
                  "in" -> JsString("path"),
                  "description" -> JsString("name that need to be updated"),
                  "type" -> JsString("string"),
                  "required" -> JsBoolean(true)
                ),
                JsObject(
                  "name" -> JsString("body"),
                  "in" -> JsString("body"),
                  "description" -> JsString("Updated user object"),
                  "schema" -> userSchema,
                  "required" -> JsBoolean(true)
                )
              ),
              "summary" -> JsString("Updated user"),
              "responses" ->
                JsObject(
                  "400" ->
                    JsObject(
                      "description" -> JsString("Invalid user supplied")
                    ),
                  "404" ->
                    JsObject(
                      "description" -> JsString("User not found")
                    )
                )
            ),
          "delete" -> JsObject(
              "description" -> JsString(
                "This can only be done by the logged in user."),
              "tags" -> JsArray(JsString("user")),
              "operationId" -> JsString("deleteUser"),
              "produces" -> JsArray(JsString("application/xml"),
                                    JsString("application/json")),
              "parameters" -> JsArray(
                JsObject(
                  "name" -> JsString("username"),
                  "in" -> JsString("path"),
                  "description" -> JsString(
                    "The name that needs to be deleted"),
                  "type" -> JsString("string"),
                  "required" -> JsBoolean(true)
                )
              ),
              "summary" -> JsString("Delete user"),
              "responses" ->
                JsObject(
                  "400" ->
                    JsObject(
                      "description" -> JsString("Invalid username supplied")
                    ),
                  "404" ->
                    JsObject(
                      "description" -> JsString("User not found")
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

  private val userSchema: JsObject =
    JsObject(
      "type" -> JsString("object"),
      "properties" ->
        JsObject(
          "email" ->
            JsObject(
              "type" -> JsString("string")
            ),
          "username" ->
            JsObject(
              "type" -> JsString("string")
            ),
          "userStatus" ->
            JsObject(
              "type" -> JsString("integer"),
              "format" -> JsString("int32"),
              "description" -> JsString("User Status")
            ),
          "lastName" ->
            JsObject(
              "type" -> JsString("string")
            ),
          "firstName" ->
            JsObject(
              "type" -> JsString("string")
            ),
          "id" ->
            JsObject(
              "type" -> JsString("integer"),
              "format" -> JsString("int64")
            ),
          "phone" ->
            JsObject(
              "type" -> JsString("string")
            ),
          "password" ->
            JsObject(
              "type" -> JsString("string")
            )
        )
      /*,"xml" -> // TODO
        JsObject(
          "name" -> JsString("User")
        )*/
    )

  private val orderSchema: JsObject =
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
