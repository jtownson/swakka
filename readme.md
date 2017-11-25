Swakka - Swagger for Akka-Http
==============================

Quickstart

```sbtshell
libraryDependencies += "net.jtownson" %% "swakka" % "0.1a-SNAPSHOT" 
```

Swakka is

1. A Scala library for creating Swagger definitions in a type-safe fashion.
3. Swagger support for Akka Http.

 
Swakka is not

1. A web runtime. Akka Http _is_ a web runtime and Swakka is a layer above that.
Swakka generates Swagger JSON and provides Akka Http Routes to (a) serve that JSON and 
(b) support the API in the Swagger definition.
It _adds_ to Akka Http and dovetails cleanly with Akka Http concepts.

Here's how it works...

### Swakka in five key points:
```scala
// Some akka imports ...	

// Some Swakka imports
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.routegen._
import net.jtownson.swakka.jsonprotocol._
	
object Greeter1 extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // (1) - Create a swagger-like API structure using an OpenApi case class.
  // Implement each endpoint as an Akka _Route_

  val greet: String => Route =
    name =>
      complete(HttpResponse(OK, corsHeaders, s"Hello $name!"))

  val api =
    OpenApi(
      produces = Some(Seq("text/plain")),
      paths =
      PathItem(
        path = "/greet",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[String]('name) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = greet
        )
      ) ::
        HNil
    )

  // (2) - Swakka will generate 
  //       a) a Route for the API. 
  //          This extracts the paths, parameters, headers, etc in your swagger definition 
  //          and passes them to your implementation.
  //       b) a swagger.json. This is added to the API route above.
  val route: Route = openApiRoute(
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
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET
Server: akka-http/10.0.5
Date: Thu, 16 Nov 2017 21:02:53 GMT
Content-Type: application/json
Content-Length: 476

{
  "swagger": "2.0",
  "info": {
    "title": "",
    "version": ""
  },
  "produces": ["text/plain"],
  "paths": {
    "/greet": {
      "get": {
        "parameters": [{
          "name": "name",
          "in": "query",
          "required": true,
          "type": "string"
        }],
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
jtownson@munch ~$ curl -i localhost:8080/greet?name=you
HTTP/1.1 200 OK
Server: akka-http/10.0.5
Date: Thu, 16 Nov 2017 21:04:59 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 10

Hello you!

jtownson@munch ~$ # (5) With the generated route directives matching the
jtownson@munch ~$ #     host, paths, parameters, etc of your swagger API definition,
jtownson@munch ~$ #     you can be sure that requests reaching your endpoing are valid.
jtownson@munch ~$ curl -i localhost:8080/greet
HTTP/1.1 404 Not Found
Server: akka-http/10.0.5
Date: Thu, 16 Nov 2017 21:06:43 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 50

Request is missing required query parameter 'name'
```

### Parameters:

The example above took a single ```QueryParameter[String]```. There are additionally
```QueryParameter[T]```, ```PathParameter[T]```, ```HeaderParameter[T]``` and ```BodyParameter[T]```
(plus ```MultiValued[T, Parameter[T]]``` which will be described later).

Here is an example app defining a ```PathParameter[String]```.

```scala
object Greeter2 extends App {

  // We'll define an endpoint that takes a single path parameter as a String.

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // The endpoint is then a function String => Route
  val greet: String => Route =
    name =>
      complete(HttpResponse(OK, corsHeaders, s"Hello ${name}!"))

  // where the String taken by the endpoint is the
  // value of the path param defined here
  val api =
    OpenApi(
      produces = Some(Seq("text/plain")),
      paths =
      PathItem(
        path = "/greet/{name}", // here we define the parameter's placeholder in the request path
        method = GET,
        operation = Operation(
          parameters = PathParameter[String]('name) :: HNil, // here we declare it should be passed to our endpoint
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = greet
        )
      ) ::
        HNil
    )

  val route: Route = openApiRoute(
    api,
    Some(SwaggerRouteSettings()))

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
```
The input *parameters* of your API are defined in terms of a _Params_ type parameter, which is 
a shapeless HList. In this example, it is a single path parameter that is read as a _String_.

For each of your swagger endpoints, you provide an implementation as a function, the exact
type of which is dependent on the parameters HList in the api definition. So, if for instance,
```scala
parameters = QueryParameter[Boolean] :: PathParameter[String] :: HNil
```
then the endpoint will have a dependent function type of ```Function2[Boolean, String, Route]```
or ```(Boolean, String) => Route```

The Swakka-generated outer Route contains Akka _directives_ that extract these Params from the HTTP request
(either from the query string, path, headers or request body). 

### Responses

The responses from your API are defined using a _Responses_ HList containing ```ResponseValue[_, _]``` elements. 
(Note, though, you can use a bare ResponseValue, that is not part of a HList; 
in swagger, the responses json element is an _object_ not a list. This makes it subtly different
than the parameters list, which will be serialized to a, possibly empty, json _array_). 

For example
```scala
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

1. The type of the response body. This can be any any case class. 
As for any Akka-Http app, you need a a spray ```JsonFormat``` to enable marshalling of your response.
Additionally, Swakka generates a ```SchemaWriter```, which is another ```JsonFormat``` for writing
a json schema for the case class into the swagger file.
    
2. Any headers set in the response (e.g. caching headers)

This provides a declarative, type-level approach to generating swagger response elements.

Here is an example:

```scala
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.jsonprotocol._

import spray.json._

import shapeless.{::, HNil}

case class Success(id: String)

type CacheControl = Header[String]

val responses: Responses = 
  ResponseValue[Success, CacheControl](
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

Note that ```Response[T, Headers]``` definitions do not modify the generated Akka Route (step 1), 
they only modify the swagger.json (step 2).

This means neither the scala compiler nor Akka's runtime will tell you if the response types declared in your OpenApi 
definition are in sync with the actual type returned by your endpoint implementation.
If you change the return type of an endpoint, you must _remember_ to update the OpenApi definition.

(I am working on fixing this).

### Optional Parameters

If a parameter in your API is optional then declare it using scala's _Option_, a la:

```scala
  "optional query parameters when missing" should "not cause request rejections" in {

    // Our query parameter is Option[Int].
    val f: Option[Int] => Route =
      iOption => complete(iOption map (_ => "Something") getOrElse "None")

    val api = OpenApi(paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Option[Int]]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = f)))

    val route = openApiRoute(api)

    // The q parameter is missing from the request
    // Our endpoint implementation will be called with q=None
    Get("http://localhost:8080/app/e1") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "None"
    }
    
    // The caller provides the value "Something" for q
    // The endpoint implementation will get Some("Something")
    Get("http://localhost:8080/app/e1?q=Something") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Something"
    }
  }
```

For a mandatory parameter such as ```QueryParameter[Int]('q)```

the generated swagger will list that parameter as required=true: 

```json
        "parameters": [{
          "name": "q",
          "in": "query",
          "required": true,
          "type": "integer"
          "format": "int32"
        }],
```

For an optional parameter such as ```QueryParameter[Option[Int]]('q)```

the generated swagger will be identical except that the parameter will have required=false:
```json
        "parameters": [{
          "name": "q",
          "in": "query",
          "required": false,
          "type": "integer"
          "format": "int32"
        }],
```

Note, _PathParameter_ does not support Optional values since Swagger/OpenAPI does not.
If you have a case where a URL makes sense both with and without some part of the path, 
you should *define two endpoints*.

### Other types of parameters

As mentioned above, the parameters you can define in Swakka are the same as those defined in the Swagger specification. Namely,
```QueryParameter[T]```, ```PathParameter[T]```, ```HeaderParamter[T]```,  ```BodyParameter[T]``` and ```FormFieldParameter[T]```.

Note that all the non-body parameters ()QueryParameter, PathParameter, HeaderParameter and FormFieldParameter)
all work in much the same way. Also note that Swagger limits the type of T to
the following

* String
* Float
* Double
* Int
* Long
* Boolean

Swakka only defines implicit JSON conversions for these types, so you need to stay within these bounds for your
code to compile. BodyParameter, on the other hand, works with arbitrary case classes. See below.

Swakka also defines a special case ```MultiValued[T]``` that wraps another, single valued, parameter and yields a Seq[T]
as the parameter value. This provides support for Swagger's ```collectionFormat=multi``` construct 
(an example of which can be found in the Petstore v2 swagger sample).
    
Swakka defines implicit (Spray) JsonFormats to convert the types above (and their Optional variants) into Swagger json.
To enable this, you import these conversions: 
```scala
import net.jtownson.swakka.jsonprotocol._
``` 

### Body parameters (and _SchemaWriter_)
BodyParameter[T] allows custom case class types for T 
(because Swagger allows custom models for the request body).

To enable this, your code requires an implicit spray JsonFormat for T 
(defined in just the same way that you already do with Akka-Http apps, using ```jsonFormat1```, ```jsonFormat2```,
etc).

Internally, Swakka derives two other type classes
1. A _SchemaWriter_ instance. This is a special JsonFormat that writes the Json Schema for T into the body of the swagger.json
(i.e. it introspects case class T and spits out a json schema as a String).
3. A _ConvertibleToDirective_ instance. This is an Akka-Http _Directive_ that will match, marshall and extract the request body. 

To enable this, you only need to import OpenApiJsonProtocol._, but I mention it in case you have problems with
implicits.

Here is some example code showing these two steps (taken from the Petstore2 testcase):

```scala

// SprayJsonSupport is required for Akka-Http to marshal case classes (e.g. Pet), to/from json.
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

// This defines the OpenApi model (i.e. the structure of a swagger file)
import net.jtownson.swakka.openapimodel._

// Imports JsonProtocols that convert the openapi model case classes 
// (OpenApi, Path, Operation, QueryParameter[T], PathParameter[T], ...)
// into their swagger json equivalents.
// OpenApiJsonProtocol extends akka's DefaultJsonProtocol, so you should avoid importing DefaultJsonProtocol.
import net.jtownson.swakka.jsonprotocol._

// Imports the route generation feature
import net.jtownson.swakka.routegen._

class Petstore2Spec extends FlatSpec with RouteTest with TestFrameworkInterface {

  case class Pet(
                  id: Long,
                  name: String,
                  tag: Option[String] = None)

  // Spray JsonFormat required by Akka Http
  implicit val petJsonFormat = jsonFormat3(Pet)
  
  // Swakka will generate a SchemaWriter.
  implicit val petSchemaWriter = implicitly[SchemaWriter[Pet]]

  // Then...
  // Define the API
  // Generate the API Route
  // and start the app
}
``` 

If you want to see some fully working, copy-and-pasteable code, there are implementations of the Petstore app in Swakka's
examples project and in the library unit tests. Take your pick.

### Annotating BodyParameters and Responses
You will probably want to annotate your custom model classes (either those used for requests or for responses) so that
the swagger file contains useful comments about each of the fields.

There are two ways to do this. 

The first is using a Swagger annotation called @ApiModelProperty.

```scala
// Import the swagger annotation
import io.swagger.annotations.ApiModelProperty
// Then import the json generation feature. Internally, 
// this brings into scope an instance of the ClassDoc typeclass
// which reads @ApiModelProperty annotations using reflection.
import net.jtownson.swakka.jsonprotocol._


case class A(@ApiModelProperty("some docs about foo") foo: Int)
```

The second is to create your own ```ClassDoc[T]``` and bring that into scope. 
It has a single method, called ```entries``` which returns a ```Map[String, FieldDoc]``` 
describing some or all of the fields in your model classes. If you want to return
hardcoded maps on a class by class basis, you can use an apply method on ClassDoc.
e.g.

```scala
case class A(foo: Int)

implicit val aDocs = new ClassDoc[A](Map("foo" -> FieldDoc("docs about foo")))

```

Of course, you can still annotate your case classes and, if specific ClassDoc
intances are in a closer scope they will take priority. You can use this to 
override annotation entries if, for example, they are in another project/library 
and your local semantics are differ.

### The endpoint implementation, _Params => Route_

You might want to extract details from a HTTP request but not document those details publicly in the swagger.json.
A header like ```X-Forwarded-For``` would be an example here. Because the endpoint implementation returns an inner Route,
you can add arbitrary Akka directives in the endpoint function to do this. For example

```scala
  "Endpoints" should "support private Akka directives" in {

    // This endpoint extracts x-forwarded-for
    val f: () => Route = _ =>
      optionalHeaderValueByName("x-forwarded-for") {
        case Some(forward) => complete(s"x-forwarded-for = $forward")
        case None => complete("no x-forwarded-for header set")
      }

    // but the swagger API does not document it...
    val api = OpenApi(paths =
      PathItem(
        path = "/app",
        method = GET,
        operation = Operation(endpointImplementation = f)))

    val route = openApiRoute(api)

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
and layer your OpenApi definition on top of existing Routes. 


### Route generation and CORS

When generating the Akka Route, you can pass a couple of options for the swagger endpoint:

```scala
import net.jtownson.swakka.routegen._
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

Note, if your Swagger API definition includes a ```basePath```, this will be prefixed to the URL for the swagger file.

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

Security definitions are optional in your swagger definition and they default to ```None```.

Otherwise, the security definition section takes the form of Some Shapeless _extensible record_ 
(https://github.com/milessabin/shapeless/wiki/Feature-overview:-shapeless-2.0.0#extensible-records).

If you are not familiar with Shapeless, extensible records will seem technical but the actual code you need in Swakka
is simple enough. If your code does not compile, you need to understand why and you are new to shapeless, 
you'll need to set aside a few hours for reading. If you are new to _Scala_, make that a few weeks!

Here is an example from the Petstore

```scala

    // In addition to the usual Swakka imports, we need bits of shapeless
    import shapeless.record._
    import shapeless.syntax.singleton._
    import shapeless.{::, HNil}
    
    // If you wish, you can make the SecurityDefinitions type explicit using this somewhat curious construct,
    // though the compiler can infer it for you.
    type SecurityDefinitions = Record.`'petstore_auth -> Oauth2ImplicitSecurity, 'api_key -> ApiKeyInHeaderSecurity`.T

    // Create a shapeless record for the security schemes
    val securityDefinitions =
      'petstore_auth ->> Oauth2ImplicitSecurity(
        authorizationUrl = "http://petstore.swagger.io/oauth/dialog",
        scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets"))) ::
      'api_key ->> ApiKeyInHeaderSecurity("api_key") ::
      HNil


    val petstoreApi = OpenApi(
      // 1. define the list of all security schemes supported by the API 
      securityDefinitions = Some(securityDefinitions),
      paths =
        PathItem(
          path = "/pets",
          method = GET,
          operation = Operation(
            // 2. reference one or more of these security schemes in an endpoint
            security = Some(Seq(SecurityRequirement('petstore_auth, Seq("read:pets")))),
            // Note, that the compiler infers akka Routes as StandardRoute, which breaks implicit resolution
            // Therefore you often need to define the endpoint return type explicitly
            endpointImplementation = () => pass: Route 
          )
        ) :: HNil
    )

```

### Imports and implicits

Before reading further it's worth having a look at the code in the Petstore apps and unit tests. There is a V1 example
that declares highlights swagger v1 concepts and a more complex V2 example that demonstrates oAuth, api_key security
and a wider array of endpoints.
  
Once you get a feel for those, here is a checklist of the implicit values (and other key objects) that you need to import or create.

* The JsonFormats to convert the OpenApi model classes (to swagger json)
```scala
import net.jtownson.swakka.jsonprotocol._
```
* The RouteGen instances to convert the API definition to an Akka Route
that will match and extract parameters, etc from HTTP requests.
```scala
import net.jtownson.swakka.routegen._
```

* Imports for shapeless HLists
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

### I'd like to create my Swagger in Scala, but I don't need the Akka bit

If you want just the serialization to Swagger JSON, you can write code like this
```scala
import net.jtownson.swakka.openapimodel._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

val api = OpenApi(...)

val json: JsValue = api.toJson

println(json.prettyPrint)
```

### Examples and testcases

If you need some working code to help get started or debug a particular problem, you can look in

1. The examples project. These allow you to run up sample APIs on localhost:8080, which you can
then examine with swagger-ui or curl.
2. The library unit tests. The testcases are often pedagogical and exemplary, rather than formal tests against the swagger
specification so you should be able to find code to fit many usage scenarios. If something is missing, please get in touch.


### Troubleshooting
#### Scala compiler unable to find implicits

Tip: if you create an API definition with dozens of endpoints, the AST is very complex. 
Each parameter for an endpoint is an element of a Params HList and each endpoint in an API is part of the Paths HList
and every element requires JsonProtocol and RouteGen implicits in scope.
If it does not compile, the Scala compiler will not help you determine which part of your code has the problem. 
Problems become harder to track down with the number of parameters and endpoints declared.
You will end up having to hack your API definition down to a single endpoint and add each parameter, 
checking for compilation at each step before moving onto the next. 
Until you gain fluency, I recommend you develop your API step by step in the first place.
This should keep the compiler on your side.
  
Check the types of your parameters. Only body parameters support custom types. For QueryParameter, PathParameter and HeaderParameter
Swagger only supports Int, Long, Float, Double, Boolean and String. For other types, the implicits required to serialize them
to swagger json do not exist.

Check there are no Optional PathParameters. They are not supported by Swagger/OpenApi.

Make sure your endpoint implementation returns _Route_. It is a good idea to declare the endpoint type explicitly,
e.g. ```val f: String => Route = ...```. Any other return type will break implicit resolution for the RouteGen.

Check you have included the Swakka imports. Unless you need to get into low-level details, include the following 
imports
1) ```openapimodel._``` to bring in the swagger model case classes.
2) ```jsonprotocol._``` to enable Swagger generation
3) ```routegen._``` to enable Akka Http Route generation


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