# _Swakka - Swagger for Akka-Http_

Swakka is

1. A Scala library for creating Swagger definitions with Akka Http.
2. A nice DSL for creating webapps. 

It _adds_ to Akka Http rather than competes and dovetails cleanly with Akka Http routing concepts.
 
Here's how it works...

### Swakka in five key points:
```scala
	
object PingPong extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  type NoParams = HNil
  type StringResponse = ResponseValue[String, HNil]

  type Paths = PathItem[NoParams, StringResponse] :: HNil
  
  // (1) - Create a swagger-like API structure using an OpenApi case class.
  // Implement each endpoint as an Akka _Route_ (e.g. complete("pong"))
  val api =
    OpenApi(paths =
      PathItem[NoParams, StringResponse](
        path = "/ping",
        method = GET,
        operation = Operation[NoParams, StringResponse](
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = _ => complete("pong") // this is the implementation. 
          // It is a function from NoParams to an Akka Http Route.
        )
      ) ::
        HNil
    )

  // (2) - Swakka will generate 
  //       a) a Route for the API. 
  //          This extracts the paths, parameters, headers, etc in your swagger definition 
  //          and passes them to your implementation.
  //       b) a swagger.json. This is added to the API route above.
  val route: Route = RouteGen.openApiRoute(
    api,
    swaggerRouteSettings = Some(SwaggerRouteSettings()))

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
```

```bash
jtownson@munch ~$ # (3) Your callers can then get the swagger file
jtownson@munch ~$ curl -i localhost:8080/swagger.json
HTTP/1.1 200 OK
Server: akka-http/10.0.5
Date: Sun, 21 May 2017 22:00:55 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 302

{
  "swagger": "2.0",
  "info": {
    "title": "",
    "version": ""
  },
  "paths": {
    "/ping": {
      "get": {
        "responses": {
          "200": {
            "description": "ok",
            "schema": {
              "type": "string"
            }
          }
        }
      }
    }
  }
}

jtownson@munch ~$ # (4) and call the API
jtownson@munch ~$ curl -i localhost:8080/ping
HTTP/1.1 200 OK
Server: akka-http/10.0.5
Date: Sun, 21 May 2017 22:02:02 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 4

pong

jtownson@munch ~$ # (5) With the generated route directives matching the
jtownson@munch ~$ #     host, paths, parameters, etc of your swagger API definition.
jtownson@munch ~$ curl -i localhost:8080/pang
HTTP/1.1 404 Not Found
Server: akka-http/10.0.5
Date: Sun, 21 May 2017 22:09:14 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 42

The requested resource could not be found.
```

### Parameters:

APIs without inputs are never very interesting (counter examples, please).
Enter ```QueryParameter[T]```, ```PathParameter[T]```, ```HeaderParameter[T]``` and ```BodyParameter[T]```.

```scala

  // We'll define an endpoint that takes a single query parameter as a String.
  type Params = QueryParameter[String] :: HNil
  type StringResponse = ResponseValue[String, HNil]

  type Paths = PathItem[Params, StringResponse] :: HNil

  // The endpoint is then a function Params => Route
  val greet: Params => Route = {
    // Pattern match the Params HList
    case (QueryParameter(name) :: HNil) =>
      complete(s"Hello ${name}!")
  }

  val api =
    OpenApi(paths =
      PathItem(
        path = "/greet",
        method = GET,
        operation = Operation[Params, StringResponse](
          parameters = QueryParameter[String]('name) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = greet
        )
      ) ::
        HNil
    )
```
The input *parameters* of your API are defined in terms of a _Params_ type parameter, which is 
a shapeless HList. In this example, it is a single query parameter that is read as a _String_.

For each of your swagger endpoints, you provide an implementation as a function
```scala
endpointImplementation: Params => Route
```

The Swakka-generated outer Route contains Akka _directives_ that extract these Params from the HTTP request
(either from the query string, path, headers or request body). 

You can then pattern match the Params HList to get the value of each ```QueryParameter```, ```PathParameter```, etc.

### Responses

The responses from your API are defined using a _Responses_ HList. 

For example
```scala
type EndpointResponses = ResponseValue[String, HNil] :: ResponseValue[Pet, HNil] :: ResponseValue[Error, HNil] :: HNil

val responses = 
      ResponseValue[String, HNil](
        responseCode = "404",
        description = "Pet not found with the id provided. Response body contains a String error message."
      ) ::
      ResponseValue[Pet, HNil](
        responseCode = "200",
        description = "Pet returned in the response body"
      ) ::
      ResponseValue[Error, HNil](
        responseCode = "500",
        description = "There was an error. Response will contain an Error json object to help debugging."
      ) ::
      HNil

```

Each ```ResponseValue``` takes two type parameters:

1. The type of the response body. This can be any any case class. You make it work you need two things

    1.1. a spray ```JsonFormat``` so that Akka Http can marshall it correctly.
    
    1.2. a Swakka ```SchemaWriter``` so that Swakka can write a json schema for the case class into the swagger file.
    
2. Any headers set in the response (e.g. caching headers)

Here is an example:

```scala
import net.jtownson.swakka.model.Responses.{Header, ResponseValue}
import net.jtownson.swakka.jsonschema.SchemaWriter._
import shapeless.{::, HNil}
import spray.json._

case class Success(id: String)

implicit val successSchema: SchemaWriter[Success] = schemaWriter(Success)

type Headers = Header[String]
type Responses = ResponseValue[Success, Headers] :: HNil

val responses: Responses = 
  ResponseValue[Success, Headers](
    responseCode = "200", 
    description = "ok",
    headers = Header[String](Symbol("cache-control"), Some("a cache control header specifying the max-age of the entity")))
    
println(responses.toJson)    

```

This will output the following Swagger snippet
```json
{
  "200": {
    "description": "ok",
    "headers": {
      "cache-control": {
        "type": "string",
        "description": "a cache control header specifying the max-age of the entity"
      }
    },
    "schema": {
      "type": "object",
      "required": ["id"],
      "properties": {
        "id": {
          "type": "string"
        }
      }
    }
  }
}
```  

Given your OpenApi definition, Swakka creates two things:
1. An Akka Route
2. A swagger.json

NB: the *response definitions do not modify the generated Akka Route in (step 1), they only modify how the swagger.json (step 2).
This means neither the scala compiler nor Akka's runtime will tell you if the response types declared in your OpenApi definition
are in sync with the actual type returned by your endpoint implementation. If you change the return type of an endpoint, you
must _remember_ to update the OpenApi definition.


### Optional Parameters

If a parameter in your API is optional then declare it using scala's _Option_, a la:

```scala
  "optional query parameters" should "not cause request rejections" in {

    // Our query parameter is Option[Int].
    type Params = QueryParameter[Option[Int]] :: HNil
    type Responses = ResponseValue[String, HNil]
    type Paths = PathItem[Params, Responses]

    val f: Params => Route = {
      // matches when the caller does provide a value
      case QueryParameter(Some(value)) :: HNil =>
        complete(value)
      // matched when the caller does not
      case QueryParameter(None) :: HNil =>
        complete("None")
    }

    val api = OpenApi[Paths, Nothing](paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Option[Int]]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    // The q parameter is missing from the request
    // Our endpoint implementation will be called with q=None
    Get("http://localhost:8080/app/e1") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "None"
    }
    
    // The caller provides the value "Something" for q
    // The endpoint implementation will get that value
    Get("http://localhost:8080/app/e1?q=Something") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Something"
    }
  }
```

For a mandatory parameter such as 
```scala
QueryParameter[Int]('q)
```

the generated swagger will list that parameter as required=true: 

```json
        "parameters": [{
          "name": "name",
          "in": "query",
          "required": true,
          "type": "integer"
          "format": "int32"
        }],
```

For an optional parameter such as
```scala
QueryParameter[Option[Int]]('q)
```

the generated swagger will be identical except that the parameter will have required=false:
```json
        "parameters": [{
          "name": "name",
          "in": "query",
          "required": false,
          "type": "integer"
          "format": "int32"
        }],
```

Note, _PathParameter_ does not support Optional values. Nor does Swagger/OpenAPI. If you have a case where
a URL makes sense both with and without some part of the path, you should *define two endpoints*.

### Other types of parameters

The sorts of parameters you can define in Swakka are the same as those defined in the Swagger specification. Namely,
```QueryParameter[T]```, ```PathParameter[T]```, ```HeaderParamter[T]``` and ```BodyParameter[T]```.

QueryParameter, PathParameter and HeaderParameter all work in the same way. Note that Swagger limits the type T to
the following

* String
* Float
* Double
* Int
* Long
* Boolean

Swakka defines implicit (Spray) JsonFormats to convert all of these types (and their Optional variants) into Swagger json.
You just need to import these conversions: 
```scala
import net.jtownson.swakka.OpenApiJsonProtocol._
``` 
Defining a PathParameter requires code like this:
```scala

  // The endpoint will take one input, a string path parameter.
  type Params = PathParameter[String] :: HNil

  // Pattern match to get its value out of the Params HList.
  val greet: Params => Route = {
    case (PathParameter(name) :: HNil) =>
      complete(HttpResponse(OK, corsHeaders, s"Hello ${name}!"))
  }

  // Create the OpenApi definition
  val api =
    OpenApi(
      produces = Some(Seq("text/plain")),
      paths =
      PathItem(
        path = "/greet/{name}", // NB! This token matches the name of the parameter below.
        method = GET,
        operation = Operation[Params, StringResponse](
          parameters = PathParameter[String]('name) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = greet
        )
      ) ::
        HNil
    )

   // generate the route and start the webserver...
``` 

### Body parameters (and _SchemaWriter_)
BodyParameter[T] allows custom (i.e. case class) types for T (because Swagger allows custom models for the request body).

For this to work, your code must create 

1. An implicit JsonFormat for T (in just the same way that you already do with Akka-Http apps)
2. A _SchemaWriter_. This writes the Json Schema for T into the body of the swagger.json
(i.e. it introspects case class T and spits out a json schema as a String). Swakka provides a function to do
this automatically. You have to call it.
3. A _ConvertibleToDirective_ instance. Swakka uses this to create an Akka Http _Directive_ that will
match and extract the request body. 

Here is some example code showing these two steps (taken from the Petstore2 testcase):

```scala

// SprayJsonSupport is required for Akka-Http to marshal case classes (e.g. Pet), to/from json.
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

// SchemaWriter converts Pet to JsonSchema (to insert into the swagger.json)
import net.jtownson.swakka.jsonschema.SchemaWriter._

// This defines the OpenApi model (i.e. the structure of a swagger file)
import net.jtownson.swakka.OpenApiModel._

// Imports JsonProtocols that convert the openapi model case classes 
// (OpenApi, Path, Operation, QueryParameter[T], PathParameter[T], ...)
// into their swagger json equivalents.
// You do not need to import Akka's DefaultJsonProtocol here. 
// OpenApiJsonProtocol extends DefaultJsonProtocol (which itself
// includes a whole bunch of JsonFormats) 
import net.jtownson.swakka.OpenApiJsonProtocol._

// This defines vals/defs that convert the Swagger model into Akka Directives
// (which then match and extract values from the request).
import net.jtownson.swakka.routegen.ConvertibleToDirective._

class Petstore2Spec extends FlatSpec with RouteTest with TestFrameworkInterface {

  case class Pet(
                  id: Long,
                  name: String,
                  tag: Option[String] = None)

  // Spray JsonFormat required by Akka Http
  implicit val petJsonFormat = jsonFormat3(Pet)
  // SchemaWriter required by Swakka to generate json schema 
  implicit val petSchemaWriter = schemaWriter(Pet)
  // ConvertibleToDirective instance which Swakka uses to tell Akka how to extract the request body.
  // Note the types are a bit confusing. Getting them wrong would cause an implicit resolution error from scalac. 
  implicit val petBodyParamConverter: ConvertibleToDirective[BodyParameter[Pet]] = bodyParamConverter[Pet]

  // ...
}
``` 

If you want to see some fully working, copy-and-pasteable code, there are implementations of the Petstore app in Swakka's
examples project and in the library unit tests. Take your pick.

### Annotating BodyParameters and Responses
You will probably want to annotate your custom model classes (either those used for requests or for responses) so that
the swagger file contains useful comments about each of the fields.

Swakka currently allows you to do this using an existing Swagger annotation called @ApiModelProperty.

```scala
import io.swagger.annotations.ApiModelProperty

case class A(@ApiModelProperty(name = "the name", value = "the value", required = true) foo: Int)
```

(Depending on feedback, I may introduce a pluggable scheme to provide this data without annotations).


### The endpoint implementation, _Params => Route_

You might want to extract details from a HTTP request but not document those details publicly in the swagger.json.
A header like ```X-Forwarded-For``` would be an example here. Because the endpoint implementation returns an inner Route,
you can add arbitrary Akka directives in the endpoint function to do this. For example

```scala
  "Endpoints" should "support private Akka directives" in {

    type Params = HNil

    // This endpoint extracts x-forwarded-for
    val f: Params => Route = _ =>
      optionalHeaderValueByName("x-forwarded-for") {
        case Some(forward) => complete(s"x-forwarded-for = $forward")
        case None => complete("no x-forwarded-for header set")
      }

    // but the swagger API does not document it...
    val api = OpenApi(paths =
      PathItem(
        path = "/app",
        method = GET,
        operation = Operation[HNil, HNil](endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    // Make a request with x-forwarded-for
    Get("http://localhost:8080/app").withHeaders(RawHeader("x-forwarded-for", "client, proxy1")) ~> seal(route) ~> check {
      responseAs[String] shouldBe "x-forwarded-for = client, proxy1"
    }

    // Make one without
    Get("http://localhost:8080/app") ~> seal(route) ~> check {
      responseAs[String] shouldBe "no x-forwarded-for header set"
    }
  }
```

This feature of Swakka's design makes it easy to integrate with existing Akka Http apps
because you can layer your OpenApi definition on top of existing Routes. 


### Route generation and CORS

When generating the Akka Route, you can pass a couple of options for the swagger endpoint:

```scala
import net.jtownson.swakka.RouteGen._
import net.jtownson.swakka.routegen.SwaggerRouteSettings
import net.jtownson.swakka.routegen.CorsUseCases._
import akka.http.scaladsl.model.headers.RawHeader
    
val corsHeaders = Seq(
  RawHeader("Access-Control-Allow-Origin", "*"),
  RawHeader("Access-Control-Allow-Methods", "GET"))

val route = openApiRoute(
  api, 
  Some(SwaggerRouteSettings(  
    endpointPath = "/path/to/my/swagger-file.json", // customize the swagger URL 
    corsUseCase = SpecificallyThese(corsHeaders)))) // customize the CORS headers (so you can use swagger-ui)

``` 

### API security

Swakka supports all of the Swagger security types. Namely

1. HTTP basic authentication.
2. Api key, where the key is set in a query parameter (note, this is usually a bad idea. Your client's api keys
will get logged by webservers and those logs copied around).
3. Api key where the key is set in a header. 
4. oAuth2 authorization code (Swagger calls this _access code security_).
5. oAuth2 implicit.
6. oAuth2 resource owner credentials (Swagger calls this _password security_).
7. oAuth2 client credentials (Swagger calls this _application security_).

When writing a swagger file manually, you define the list of security schemes supported by your API and then
reference one of those schemes (by name) for each endpoint. Swakka is the same, but in Scala.

Security definitions are optional in your swagger definition and by default they are skipped:
```scala
type SecurityDefinitions = Nothing

// By allowing securityDefinitions to take the value of None (which is the default value)
// The type of SecurityDefinitions is Nothing
val uselessApi = OpenApi[HNil, SecurityDefinitions](paths = HNil, securityDefinitions = None)

// is long hand for
val sameUselessApi = OpenApi(paths = HNil)
```

Otherwise, the security definition section takes the form of a Shapeless _extensible record_ 
(https://github.com/milessabin/shapeless/wiki/Feature-overview:-shapeless-2.0.0#extensible-records).

Extensible records can get technical but the actual code you need in Swakka is simple enough. But, if your code
does not compile, you need to understand why and you are new to shapeless, you'll need to set aside a few hours for reading.
If you are new to _Scala_, make that a few weeks!

Here is an example from the Petstore

```scala

    // In addition to the usual Swakka imports, we need bits of shapeless
    import shapeless.record._
    import shapeless.syntax.singleton._
    import shapeless.{::, HNil}
    
    
    // This curious construct is the simplest way to declare the static type of a SecurityDefinition
    // Usually, the Scala compiler can infer it for you but making it explicit gives really good readability. Other
    // devs looking at your code can see the API security at a glance.
    type SecurityDefinitions = Record.`'petstore_auth -> Oauth2ImplicitSecurity, 'api_key -> ApiKeyInHeaderSecurity`.T

    // Create a shapeless record for the security schemes
    val securityDefinitions =
      'petstore_auth ->> Oauth2ImplicitSecurity(
        authorizationUrl = "http://petstore.swagger.io/oauth/dialog",
        scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets"))) ::
      'api_key ->> ApiKeyInHeaderSecurity("api_key") ::
      HNil


    val petstoreApi = OpenApi[Paths, SecurityDefinitions](
      // 1. define the list of all security schemes supported by the API 
      securityDefinitions = Some(securityDefinitions),
      paths =
        PathItem[HNil, HNil](
          path = "/pets",
          method = GET,
          operation = Operation(
            // 2. reference one or more of these security schemes in an endpoint
            security = Some(Seq(SecurityRequirement('petstore_auth, Seq("read:pets")))),
            endpointImplementation = _ => ???
          )
        ) :: HNil
    )

```

### Imports and implicits

Before reading further it's worth having a look at the code in the Petstore apps and unit tests. There is a V1 example
that declares a simple API for posting and getting Pets and a more complex V2 example that demonstrates oAuth, api_key security
and a wider array of endpoints.
  
Once you get a feel for those, here is a checklist of the implicit values (and other key objects) that you need to import or create.

* The JsonFormats to convert the OpenApi model classes (to swagger json)
```scala
import net.jtownson.swakka.OpenApiJsonProtocol._
```
where N is 1, 2, 3, according to the number of fields in the case class.
* For a custom case class, T, the SchemaWriter needed to write T's json schema into the swagger file
```scala
import net.jtownson.swakka.jsonschema.SchemaWriter._
implicit val schemaWriter = schemaWriter(T)
```
* For custom request body types (for POST and PUT requests),
the RouteGen instances to convert case class T to an Akka Directive
that will match and extract instances of T from HTTP requests.
```scala
  import net.jtownson.swakka.routegen.ConvertibleToDirective._
  implicit val bodyParamConverter: ConvertibleToDirective[BodyParameter[T]] = bodyParamConverter[T]
```

Note the type parameters on the left and right of the expression above. Calling bodyParamConverter with the
wrong type causes the scala compiler to emit a 'Cannot find implicit...' error which is hard to track down (I know!)

* Imports for shapeless HLists to work
```scala
import shapeless.{::, HNil}
```
And, for api security definitions, imports for extensible records
```scala
import shapeless.record._
import shapeless.syntax.singleton._
```

* Additional imports/vals required to make Akka Http work
```scala
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
implicit val jsonFormat = jsonFormatN(T) // where N is 1, 2, 3, according to the number of fields in the case class.

```

### Examples and testcases

If you need some working code to help get started or debug a particular problem, you can look in

1. The examples project. These allow you to run up sample APIs on localhost:8080, which you can
then examine with swagger-ui or curl.
2. The library unit tests. The testcases are often pedagogical and exemplary, rather than formal tests against the swagger
specification so you should be able to find code to fit many usage scenarios. If something is missing, please get in touch.


### Troubleshooting
#### Scala compiler unable to find implicits

Tip: if you create an API definition with dozens of endpoints and it does not compile, the Scala compiler is not very
helpful at telling you which part of your code has the problem. Each parameter for an endpoint is an element of a Params HList
and each endpoint in an API is part of the Paths HList. The Scala compiler will often only tell you there is a problem in a HList
but not where. Problems become harder to track down with the number of parameters and endpoints declared.
You will end up having to hack your API definition down to a single endpoint and add each parameter, 
checking for compilation at each step before moving onto the next. Until you gain fluency, I recommend you develop your API
definitions this way in the first place -- in small steps. This and the implicits checklist above should keep the compiler on your side.
  
Check the types of your parameters. Only body parameters support custom types. For QueryParameter, PathParameter and HeaderParameter
Swagger only supports Int, Long, Float, Double, Boolean and String. For other types, the implicits required to serialize them
to swagger json do not exist.

Check there are no Optional PathParameters.
 
#### Akka rejects requests for the swagger file

Check that

1. The host header in the request matches any host declared in the OpenApi definition.
2. The scheme (http/https) matches any schemes declared in the OpenApi definition.
3. The base path in the URL matches an basePath declared in the OpenApi definition.
4. Obviously check the URL is correct too.

If all these line up, the request will be accepted. The port does not matter.  

#### Akka rejects API requests

The simplest thing is to get the swagger route working (see above) and take a look at the generated swagger file.
Remember, Swakka Routes do not check responses so the problem is always that something in the _request_ does not
match the API definition.

Check the case of query and path parameters. They are case sensitive whereas headers are not.

Check that parameters declared with required=true (i.e. non-option types) are all present.

Finally, as for the swagger file, check the host header, scheme, base path and url all marry up with the api definition.