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
package net.jtownson.swakka.openapimodel

sealed trait Parameter[T] {
  def name: Symbol

  def description: Option[String]

  def default: Option[T]
}

trait CollectionFormat {
  def delimiter: Char
}
case object multi extends CollectionFormat {
  override def delimiter = '\u0000'
}
case object csv extends CollectionFormat {
  override def delimiter = ','
}
case object ssv extends CollectionFormat {
  override def delimiter = ' '
}
case object tsv extends CollectionFormat {
  override def delimiter = '\t'
}
case object pipes extends CollectionFormat {
  override def delimiter = '|'
}

case class MultiValued[T, U <: Parameter[T]](singleParam: U,
                                             collectionFormat: CollectionFormat = multi,
                                             default: Option[Seq[T]] = None)
    extends Parameter[Seq[T]] {
  override def name: Symbol = singleParam.name

  override def description: Option[String] = singleParam.description
}

case class FormFieldParameter[T](name: Symbol,
                                 description: Option[String] = None,
                                 default: Option[T] = None)
    extends Parameter[T]

case class QueryParameter[T](name: Symbol,
                             description: Option[String] = None,
                             default: Option[T] = None)
    extends Parameter[T]

case class PathParameter[T](name: Symbol,
                            description: Option[String] = None,
                            default: Option[T] = None)
    extends Parameter[T]

case class BodyParameter[T](name: Symbol,
                            description: Option[String] = None,
                            default: Option[T] = None)
    extends Parameter[T]

case class HeaderParameter[T](name: Symbol,
                              description: Option[String] = None,
                              default: Option[T] = None)
    extends Parameter[T]


case class PathParameterConstrained[T, U](name: Symbol,
                                       description: Option[String] = None,
                                       default: Option[T] = None,
                                       constraints: Constraints[U])
  extends Parameter[T]

case class HeaderParameterConstrained[T, U](name: Symbol,
                              description: Option[String] = None,
                              default: Option[T] = None,
                              constraints: Constraints[U])
  extends Parameter[T]

case class QueryParameterConstrained[T, U](name: Symbol,
                              description: Option[String] = None,
                              default: Option[T] = None,
                              constraints: Constraints[U])
  extends Parameter[T]


case class FormFieldParameterConstrained[T, U](name: Symbol,
                                 description: Option[String] = None,
                                 default: Option[T] = None,
                                 constraints: Constraints[U])
  extends Parameter[T]
