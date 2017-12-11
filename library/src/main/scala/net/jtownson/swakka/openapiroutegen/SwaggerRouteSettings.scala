package net.jtownson.swakka.openapiroutegen

import net.jtownson.swakka.coreroutegen.CorsUseCase

case class SwaggerRouteSettings(endpointPath: String = "/swagger.json", corsUseCase: CorsUseCase = NoCors)
