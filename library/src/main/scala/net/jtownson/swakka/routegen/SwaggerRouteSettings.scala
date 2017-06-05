package net.jtownson.swakka.routegen

import net.jtownson.swakka.routegen.CorsUseCases.NoCors

case class SwaggerRouteSettings(endpointPath: String = "/swagger.json", corsUseCase: CorsUseCase = NoCors)
