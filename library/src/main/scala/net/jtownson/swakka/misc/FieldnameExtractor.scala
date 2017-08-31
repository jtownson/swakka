/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
