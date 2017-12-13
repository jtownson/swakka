package net.jtownson.swakka.coreroutegen

case class DocRouteSettings(endpointPath: String = "/swagger.json", corsUseCase: CorsUseCase = NoCors)
