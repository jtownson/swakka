package net.jtownson.swakka.routegen

case class SwaggerRouteSettings(endpointPath: String = "/swagger.json", corsUseCase: CorsUseCase = NoCors)
