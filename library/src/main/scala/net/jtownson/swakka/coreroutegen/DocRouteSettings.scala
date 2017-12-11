package net.jtownson.swakka.coreroutegen

import net.jtownson.swakka.openapiroutegen.NoCors

case class DocRouteSettings(endpointPath: String = "/swagger.json", corsUseCase: CorsUseCase = NoCors)
