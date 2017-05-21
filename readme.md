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
  
  // (1) - Create a swagger-like API structure.
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

  // (2) - Swakka generates a route for the API and an additional route for the swagger.json.
  val route: Route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
```

```bash
jtownson@munch ~$ # (3) Get the swagger file
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

jtownson@munch ~$ # (4) Call the API
jtownson@munch ~$ curl -i localhost:8080/ping
HTTP/1.1 200 OK
Server: akka-http/10.0.5
Date: Sun, 21 May 2017 22:02:02 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 4

pong

jtownson@munch ~$ # (5) The Swakka generated route contains directives that match the 
jtownson@munch ~$ #     host, paths, parameters, etc of your swagger API definition.
jtownson@munch ~$ curl -i localhost:8080/pang
HTTP/1.1 404 Not Found
Server: akka-http/10.0.5
Date: Sun, 21 May 2017 22:09:14 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 42

The requested resource could not be found.
```

### Some other key details:

```scala
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
The input *parameters* of your API are defined in terms of a type parameter, which is 
a shapeless HList. In this example, it is a single query parameter that is read as a _String_.

For each of your swagger endpoints, you provide an implementation as a function from
```scala
endpointImplementation: Params => Route
```
