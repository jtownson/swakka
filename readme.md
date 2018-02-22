Swakka - Swagger for Akka-Http
==============================

Quickstart

```sbtshell
libraryDependencies += "net.jtownson" %% "swakka" % "0.51"
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
import net.jtownson.swakka.openapijson._
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen._
	
object Greeter1 extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val corsHeaders = Seq(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET"))

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
          parameters = Tuple1(QueryParameter[String]('name)),
          responses = ResponseValue[String]("200", "ok"),
          endpointImplementation = greet
        )
      )
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

The example above took a single ```QueryParameter[String]```. 
There are in fact

- ```QueryParameter[T]```
- ```PathParameter[T]```
- ```HeaderParameter[T]```
- ```BodyParameter[T]```
- ```MultiValued[T, Parameter[T]]``` this is a wrapper for handling multiple valued query params.

In OpenApi, the set of parameters for an endpoint is given as a JSON array. In Swakka,
you use any ```Product``` type. Specifically, you can use

- a scala tuple
```scala
parameters =
 (
      PathParameter[Long](
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
      )
  )
```
- a case class 
```scala
case class Parameters(
    petId: PathParameter[Long], 
    additionalMetadata: FormFieldParameter[Option[String]],
    file: FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]])

// ...

parameters = Parameters(
  PathParameter[Long](
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
  ))
``` 

- a shapeless HList

```scala
import shapeless.{HNil, ::}
// ...
parameters = 
  PathParameter[Long](
    name = 'petId,
    description = Some("ID of pet to update")
        )
  ::
  FormFieldParameter[Option[String]](
    name = 'additionalMetadata,
    description = Some("Additional data to pass to server")
  )
  ::
  FormFieldParameter[Option[(FileInfo,
                             Source[ByteString, Any])]](
    name = 'file,
    description = Some("file to upload")
  )
  ::
        HNil
```


The parameters Product type of each endpoint in your API defines a _Params_ type parameter.
See ```net.jtownson.swakka.openapimodel.Operation```.

For each of your swagger endpoints, you provide an endpoint implementation function to handle the request.
The function type of this endpoint implementation is dependent on the
Params type definition. If for instance,

```scala
Params = (QueryParameter[Boolean], PathParameter[String], HeaderParameter[Long])
```

then the endpoint implementation will have a dependent function type of ```(Boolean, String, Long) => Route```

Swakka reads the Params defined in your API and generates Akka-Http
Routes that extract those Params. It passes those params to your endpoint implementation.
The Route returned from your endpoint implementation is then a nested, inner Route
which completes the response. 


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
          responses = Response[String]("200", "ok"),
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

### Constrained parameters

JsonSchema provides a fairly wide array of _validation constraints_. For example,
it allows you to specify that an int parameter must be >0 or that a string parameter must match a regex.
(All the options are here http://json-schema.org/latest/json-schema-validation.html#rfc.section.6.2.3).

By extension OpenApi allows such constraints on parameter definitions.

To support this, Swakka provides an additional set of types called

* QueryParameterConstrained[T, U]
* PathParameterConstrained[T, U]
* FormFieldParameterConstrained[T, U]
* HeaderParameterConstrained[T, U]

Here, _T_ is the type of the parameter itself. _U_ refers to the type of the constraint. So, for example 
```scala
QueryParameterConstrained[Option[String], String](
    name = 'state,
    default = Some("open"),
    constraints = Constraints(enum = Some(Set("open", "closed"))))
```

This code can be read as:

* This is an optional, string query parameter, called _state_. Type T = Option[String]
* If the state param is missing from the request, there is a default value: _open_
* The constraints are on the String value itself (they are orthogonal to the parameter being optional). Therefore U = String.
* A valid URL would be ?state=closed. An invalid url would be ?state=bam


### Notes on using parameters

As mentioned above, the parameters you can define in Swakka are the same as those defined in the Swagger specification. Namely,
```QueryParameter[T]```, ```PathParameter[T]```, ```HeaderParamter[T]```,  ```BodyParameter[T]``` and ```FormFieldParameter[T]```.

Note that the non-body parameters (QueryParameter, PathParameter, HeaderParameter and FormFieldParameter)
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

OpenApi also allows its query, header and form parameters to be multivalued. Thus json arrays (and therefore Scala Seqs)
of the above types are also possible. To support this, Swakka defines a special case ```MultiValued[T]``` that wraps another,
single valued, parameter and yields a Seq[T] as the parameter value. This extra type is unfortunate but necessary because
OpenApi allows several collection format, namely ```[multi|csv|pipes|ssv|tsv]```. There is an example of ```MultiValued[T]```
in the Petstore v2 swagger sample.
    
Swakka defines implicit (Spray) JsonFormats to convert the types above (and their Optional variants) into Swagger json.
To enable this, you import these conversions: 
```scala
import net.jtownson.swakka.openapijson._
``` 

### Body parameters (and _SchemaWriter_)

(Unlike above) BodyParameter[T] *does* allow custom case class types for T 
(because Swagger allows custom models for the request body).

To enable this, your code requires an implicit spray JsonFormat for T 
(defined in just the same way that you already do with Akka-Http apps, using ```jsonFormat1```, ```jsonFormat2```,
etc).

Internally, Swakka derives two other type classes

1. A _SchemaWriter_ instance. This is a special JsonFormat that writes the Json Schema for T into the body of the swagger.json
(i.e. it introspects case class T and spits out a json schema as a String).

2. A _ConvertibleToDirective_ instance. This is an Akka-Http _Directive_ that will match, marshall and extract the request body. 

To enable this, you only need to import 

```scala
net.jtownson.swakka.openapijson._
net.jtownson.swakka.coreroutegen._
net.jtownson.swakka.openapiroutegen._
```

but it is worth checking this in case you have problems with implicits.

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
import net.jtownson.swakka.openapijson._

// Imports the route generation feature
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen._

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

### Responses

The Swagger response declarations for your API are defined in a similar fashion to parameters, except responses comprise 
a Product of ```ResponseValue[_, _]``` elements. 

For example, as a HList.

```scala
val responses = 
  ResponseValue[String](
    responseCode = "404",
    description = "Pet not found with the id provided. Response body contains a String error message."
  ) ::
  ResponseValue[Pet](
    responseCode = "200",
    description = "Pet returned in the response body"
  ) ::
  ResponseValue[Error](
    responseCode = "500",
    description = "There was an error. Response will contain an Error json object to help debugging."
  ) ::
  HNil
```

or equivalently a tuple

```scala
val responses = 
(
  ResponseValue[String](
    responseCode = "404",
    description = "Pet not found with the id provided. Response body contains a String error message."
  ),
  ResponseValue[Pet](
    responseCode = "200",
    description = "Pet returned in the response body"
  ),
  ResponseValue[Error](
    responseCode = "500",
    description = "There was an error. Response will contain an Error json object to help debugging."
  )
)
```

Each ```ResponseValue``` takes two type parameters:

1. The type of the response body. This response body type can be a Swagger native type
(String, Boolean, Float, Double, Int, or Long), a case class or a Seq or Map built from these
types). A handful of other types are also supported, such as Akka's DateTime.
 
The main requirement from a Scala point of view is that, for these types, the compiler can find 
a spray ```JsonFormat``` to enable marshalling of your response (as for any Akka-Http app), plus a Swakka 
```SchemaWriter```. SchemaWriter is an extension to ```JsonFormat``` for writing a json schema for the response type
(and body parameters) into the swagger file. Swakka provides SchemaWriter instances for all the types mentioned above
(including arbitrary case classes). For any other type, use can write a custom SchemaWriter (in
a similar fashion to the way you would provide a custom Spray JsonFormat). See Petstore2Spec in
the codebase for an example.
    
2. Any headers set in the response (e.g. caching headers). See the example below.

This provides a declarative, type-level approach to generating swagger response elements.

Here is an example to show how it works:

```scala
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapijson._

import spray.json._

case class Success(id: String)

type CacheControl = Header[String]

// This is a responses definition, which you can include in a wider API definition
val responses: Responses = 
  ResponseValue[Success, CacheControl](
    responseCode = "200", 
    description = "ok",
    headers = Header[String](Symbol("cache-control"), Some("a cache control header specifying the max-age of the entity")))
    
// Or just print directly
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

Remember that, given your OpenApi definition, Swakka creates two things:

1. An Akka Route. This extracts declared request parameters and passes them to your endpoint functions.
2. A swagger.json to provide your api definition to the world outside.

Note that ```Response[T, Headers]``` definitions do not modify the generated Akka Route (step 1), 
they only modify the swagger.json (step 2).

This means neither the scala compiler nor Akka's runtime will tell you if the response types declared in your OpenApi 
definition are in sync with the actual type returned by your endpoint functions.
If you change the return type of an endpoint, you must _remember_ to update the OpenApi definition.

(I am considering options to fix this).


### Annotating BodyParameters and Responses
For your custom request and response types, you will often want to provide useful documentation about their fields.
There are two ways to do this. 

The first is using a Swagger annotation called @ApiModelProperty.

```scala
// Import the swagger annotation
import io.swagger.annotations.ApiModelProperty
// Then import the json generation feature. Internally, 
// this brings into scope an instance of the ClassDoc typeclass
// which reads @ApiModelProperty annotations using reflection.
import net.jtownson.swakka.openapijson._


case class A(@ApiModelProperty("some docs about foo") foo: Int)
```

Swakka will read these annotations and use them to derive an implicit ```ClassDoc[A]```.
This is really just a type-level wrapper around a ```Map[String, FieldDoc]``` where the keys correspond
to the names of the fields in the case class that are documented.

If you want to avoid annotations, you have a second option, which is to create your own implicit ```ClassDoc[T]```
instance and bring that into scope. 

```ClassDoc[T]``` has a single method, called ```entries``` which returns a ```Map[String, FieldDoc]``` 
describing some or all of the fields in a model class. The ```ClassDoc``` companion object provides
an apply method to turn a ```Map[String, FieldDoc]``` directly into a ```ClassDoc``` instance.
e.g.

```scala
case class A(foo: Int)

implicit val aDocs = new ClassDoc[A](Map("foo" -> FieldDoc("docs about foo")))

```

Of course, you can still annotate your case classes and, if specific ClassDoc
instances are in a closer scope they will take priority. You can use this to 
override annotation entries if, for example, they are in another project/library 
and your local semantics differ.

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
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen._
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
i.e. if you start your app and /swagger.json gives you a 404, remember you might need to add the API base path.

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

Otherwise, the security definition section takes the form of a Product, just like Parameters or Responses.

Here is an example from the Petstore

```scala
    
    // Create a shapeless record for the security schemes
    val securityDefinitions =
      (
      Oauth2ImplicitSecurity(
        key = "petstore_auth",
        authorizationUrl = "http://petstore.swagger.io/oauth/dialog",
        scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets"))),
        
      ApiKeyInHeaderSecurity("api_key")
      )

    val petstoreApi = OpenApi(
      // 1. define the list of all security schemes supported by the API 
      securityDefinitions = Some(securityDefinitions),
      paths = Tuple1(
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
        ))
    )

```

### Imports and implicits

Before reading further it's worth having a look at the code in the Petstore apps and unit tests. There is a V1 example
that declares highlights swagger v1 concepts and a more complex V2 example that demonstrates oAuth, api_key security
and a wider array of endpoints.
  
Once you get a feel for those, here is a checklist of the implicit values (and other key objects) that you need 
to import or create.

* The JsonFormats to convert the OpenApi model classes (to swagger json)
```scala
import net.jtownson.swakka.openapijson._
```
* The RouteGen instances to convert the API definition to an Akka Route
that will match and extract parameters, etc from HTTP requests.
```scala
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen._
```

* Imports for shapeless HLists, if you are defining your API using the shapeless style for Product definitions.
```scala
import shapeless.{::, HNil}

```

* Additional imports/vals required to make Akka Http work
```scala
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
implicit val jsonFormat = jsonFormatN(T) // where N is 1, 2, 3, according to the number of fields in the case class.
// other akka directives you need for your endpoint functions...
```

### I'd like to create my Swagger in Scala, but I don't need the Akka bit

If you want just the serialization to Swagger JSON, you can write code like this
```scala
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapijson._
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

Check infered types for the OpenApi case class and the inner PathItems and Operations. If you have an implicit error,
it is often worth putting in the type parameters explicitly. This can often transform a very daunting implicit error
into a relatively harmless type mismatch.

Check you have included the Swakka imports. Unless you need to get into low-level details, include the following 
imports
1) ```openapimodel._``` to bring in the swagger model case classes.
2) ```openapijson._``` to enable Swagger generation
3) ```coreroutegen._``` and
4) ```openapiroutegen._``` to enable Akka Http Route generation


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
