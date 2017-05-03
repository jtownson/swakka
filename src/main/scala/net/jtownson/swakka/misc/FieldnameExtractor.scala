package net.jtownson.swakka.misc

import scala.reflect.runtime.universe._

object FieldnameExtractor {

  def fieldNameTypes[T: TypeTag]: List[(String, String)] = {
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

    defaultConstructor.paramLists.head.map(symbol => (symbol.name.toString, symbol.typeSignature.typeSymbol.name.toString))
  }

  def fieldNames[T: TypeTag]: List[String] = fieldNameTypes[T].map(_._1)
}
