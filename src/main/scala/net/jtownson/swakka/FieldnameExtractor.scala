package net.jtownson.swakka

import scala.reflect.runtime.universe._

object FieldnameExtractor {

  def fieldNames[T: TypeTag]: List[String] = {

    val tpe = typeOf[T]

    val constructorSymbol = tpe.decl(termNames.CONSTRUCTOR)

    val defaultConstructor: MethodSymbol =
      if (constructorSymbol.isMethod) {
        constructorSymbol.asMethod
      }
      else {
        val constructors = constructorSymbol.asTerm.alternatives
        constructors.map(_.asMethod).find {
          _.isPrimaryConstructor
        }.get
      }

    defaultConstructor.paramLists.head.map(symbol => symbol.name.toString)
  }
}
