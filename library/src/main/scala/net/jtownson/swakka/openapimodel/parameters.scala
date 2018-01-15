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

  def enum: Option[Seq[T]]
}

case class MultiValued[T, U <: Parameter[T]](singleParam: U,
                                             name: Symbol,
                                             description: Option[String],
                                             default: Option[Seq[T]])
    extends Parameter[Seq[T]] {

  override def enum: Option[Seq[Seq[T]]] = None
}

object MultiValued {
  def apply[T, U <: Parameter[T]](
      singleParam: U,
      default: Option[Seq[T]] = None): MultiValued[T, U] =
    MultiValued[T, U](singleParam,
                      singleParam.name,
                      singleParam.description,
                      default)
}

case class FormFieldParameter[T](name: Symbol,
                                 description: Option[String] = None,
                                 default: Option[T] = None,
                                 enum: Option[Seq[T]] = None)
    extends Parameter[T]

case class QueryParameter[T](name: Symbol,
                             description: Option[String] = None,
                             default: Option[T] = None,
                             enum: Option[Seq[T]] = None)
    extends Parameter[T]

case class PathParameter[T](name: Symbol,
                            description: Option[String] = None,
                            default: Option[T] = None,
                            enum: Option[Seq[T]] = None)
    extends Parameter[T]

case class BodyParameter[T](name: Symbol,
                            description: Option[String] = None,
                            default: Option[T] = None,
                            enum: Option[Seq[T]] = None)
    extends Parameter[T]

case class HeaderParameter[T](name: Symbol,
                              description: Option[String] = None,
                              default: Option[T] = None,
                              enum: Option[Seq[T]] = None)
    extends Parameter[T]


case class PathParameterConstrained[T, U](name: Symbol,
                                       description: Option[String] = None,
                                       default: Option[T] = None,
                                       constraints: Constraints[U])
  extends Parameter[T] {

  override def enum: Option[Seq[T]] = ??? // TODO remove enum from the Parameter interface.
}

case class HeaderParameterConstrained[T, U](name: Symbol,
                              description: Option[String] = None,
                              default: Option[T] = None,
                              constraints: Constraints[U])
  extends Parameter[T] {

  def enum: Option[Seq[T]] = ??? // TODO remove
}

case class QueryParameterConstrained[T, U](name: Symbol,
                              description: Option[String] = None,
                              default: Option[T] = None,
                              constraints: Constraints[U])
  extends Parameter[T] {

  def enum: Option[Seq[T]] = ??? // TODO remove
}
