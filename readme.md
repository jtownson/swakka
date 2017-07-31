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
  // Implement each endpoint as a _Route_ (e.g. complete("pong"))
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

  // (2) - Swakka generates an outer route for the API, using your implementation(s) as inner routes.
  // Optionally, add an additional route for the swagger.json.
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
    case (nameParameter :: HNil) =>
      complete(s"Hello ${nameParameter.value}!")
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

For each of your swagger endpoints, you provide an implementation as a function from
```scala
endpointImplementation: Params => Route
```
The Swakka-generated route contains Akka _directives_ that extract Params 
(either in the query string, path, headers or request body) from the request. 
Pattern match the Params HList to obtain each parameter.
The _value_ field contains the extracted parameter value.

Type parameters also define endpoint Responses. Note, however, *the swagger response definitions
do not modify the generated Route* (they only change how the swagger.json will be rendered).

Swakka generates two things from your API definition:
1) An akka Route
2) A swagger.json
Parameters affect both 1 and 2. Responses only affect the swagger file.

### Optional Parameters

### API security

### Imports and implicits



### Troubleshooting
#### Scala compiler unable to find implicits

#### Akka rejects requests for the swagger file

#### Akka rejects API requests
