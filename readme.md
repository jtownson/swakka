# _Swakka - Swagger for Akka-Http_

### Swakka in five key points:
```scala
	
object PingPong extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  type NoParams = HNil
  type StringResponse = ResponseValue[String, HNil]

  type Paths = PathItem[NoParams, StringResponse] :: HNil
  
  // (1) - Create a swagger-like API structure using case classes.
  // Implement each endpoint as an Akka _Route_ (e.g. complete("pong"))
  val api =
    OpenApi(paths =
      PathItem[NoParams, StringResponse](
        path = "/ping",
        method = GET,
        operation = Operation[NoParams, StringResponse](
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = _ => complete("pong")
        )
      ) ::
        HNil
    )

  // (2) - Swakka generates an outer Route for the API (paths, parameters, headers, etc), using your implementation as an inner route.
  // Optionally, you can tell swakka to add a Route for the generated swagger.json.
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

```scala

  // We'll define an endpoint that takes a single query parameter as a String.
  type Params = QueryParameter[String] :: HNil
  type StringResponse = ResponseValue[String, HNil]

  type Paths = PathItem[Params, StringResponse] :: HNil

  val greet: Params => Route = {
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

The scala compiler then checks that the parameters in your swagger match those in your endpoint implementation.

The Swakka-generated outer Route contains Akka _directives_ that extract these Params from the HTTP request
(either from the query string, path, headers or request body). 

You can then use pattern matching to get the value of each QueryParameter, PathParameter, etc.

The responses from your API are also statically typed using a type parameter. Note, however, *the swagger response definitions
do not modify the generated Route* (they only change how the swagger.json will be rendered).

Swakka generates two things from your API definition:
1) An akka Route
2) A swagger.json
Parameters affect both 1 and 2. Responses only affect the swagger file.

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
    // Our endpoint implementation will be called with the q=None
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

### Other types of parameters

The sorts of parameters you can define in Swakka are the same as those defined in the Swagger specification. Namely,
QueryParameter[T], PathParameter[T], HeaderParamter[T] and BodyParameter[T].

QueryParameter, PathParameter and HeaderParameter all work in exactly the same way. Note that Swagger limits the type T to
the following
* String
* Float
* Double
* Int
* Long
* Boolean

Swakka defines implicit (Akka) JsonFormats to convert all of these types (and their Optional variants) into Swagger json.
You just need to import these conversions: 
```scala
import net.jtownson.swakka.OpenApiJsonProtocol._
``` 

### Body parameters (and _SchemaWriter_)
BodyParameter[T] allows custom (i.e. case class) types for T (because Swagger allows custom models for the request body).
For this to work, your code must create 
1. An implicit JsonFormat (in just the same way that you already do with Akka-Http apps)
2. A _SchemaWriter_. This is a Swakka concept. A schema writer writes the Json Schema for T into the body of the swagger.json
(i.e. it introspects case class T and spits out a json schema as a String).

Here is some example code showing these two steps (taken from the Petstore2 testcase):

```scala

// SprayJsonSupport is required for Akka-Http to marshal case class, Pet, to/from json. 
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

  type Pets = Seq[Pet]

  case class Error(
                    id: Int,
                    message: String
                  )

  implicit val petJsonFormat = jsonFormat3(Pet)
  implicit val petSchemaWriter = schemaWriter(Pet)
  implicit val petBodyParamConverter: ConvertibleToDirective[BodyParameter[Pet]] = bodyParamConverter[Pet]

  // ...
}
``` 

If you want to see some fully working, copy-and-pasteable code, there are implementations of the Petstore app in Swakka's
examples project and in the library unit tests. Take your pick.

### Imports and implicits

Before reading further it's worth having a look at the code in the PetstoreV1 samples (either the sample apps or unit test). 
Once you get a feel for that, here is a checklist of the implicit values that you need to import or create.

1. The JsonFormats to convert the Swagger model classes (to swagger json)
```scala
import net.jtownson.swakka.OpenApiJsonProtocol._  
```

2. The SchemaWriters to generate swagger schemas from your case classes
```scala
import net.jtownson.swakka.jsonschema.SchemaWriter._
``` 

3. The RouteGen instances to convert Swagger model classes to Akka Directives (which match and extract values from HTTP requests)
```scala

```
 or Have a look through the code in the Petstore samples before you read
### Route generation and CORS


### API security




### Examples and testcases

If you need some working code to help get started or debug a particular problem, you can look in
1. The examples project. These allow you to run up sample APIs on localhost:8080, which you can
then examine with swagger-ui or curl.
2. The library unit tests. The testcases are pedagogical and examplary, rather than formal tests against the swagger
specification so you should be able to find code to fit most usage scenarios. If something is missing, please get in touch and I'll 
update the testsuite.


### Troubleshooting
#### Scala compiler unable to find implicits

#### Akka rejects requests for the swagger file

#### Akka rejects API requests
