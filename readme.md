# _Swakka - Swagger for Akka-Http_


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
jtownson@munch ~$ curl -i localhost:8080/ping
HTTP/1.1 200 OK
Server: akka-http/10.0.5
Date: Sun, 21 May 2017 22:02:02 GMT
Content-Type: text/plain; charset=UTF-8
Content-Length: 4

pong
```