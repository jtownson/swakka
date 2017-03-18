package net.jtownson.openapi


import net.jtownson.openapi.OpenApiModel._

object TestDefinitions {
  val ruokMinimal = OpenApi(
    swagger = "2.0",
    info = Info(
      version = "1.0.0",
      title = "RUOK minimal"
    ),
    host = Some("localhost:8080"),
    basePath = Some("/"),
    schemes = List("http"),
    produces = List("text/plain"),
    paths = Map(
      "/ruok" ->
        PathItem(
          Get,
          Operation(
            description = Some("Returns YES if the service is running."),
            produces = List("text/plain"),
            responses = List(
              ResponseValue(
                200,
                "The string 'YES', indicating the service is running.",
                classOf[String])
            )
          ))
    )
  )
}
