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
  def description: Option[String]

  def value: T

  def default: Option[T]

  def enum: Option[Seq[T]]

  def name: Symbol
}

sealed trait ClosedParameter[T, U] extends Parameter[T]

sealed trait OpenParameter[T, U] extends Parameter[T] {
  def value: T =
    throw new IllegalStateException(
      "The parameter is currently in the state of a template " +
        "without an associated value. Parameters with values " +
        "can only be created by passing http requests through matching route structures. " +
        "(i.e. create an API definition, get its Route then make a request).")

  def closeWith(t: T): U
}

sealed trait MultiValued[T, U <: Parameter[T]] extends Parameter[Seq[T]] {
  def singleParam: U
  def enum: Option[Seq[Seq[T]]] = None
}

object MultiValued {
  def apply[T, U <: Parameter[T]](
      singleParam: U,
      default: Option[Seq[T]] = None): MultiValued[T, U] =
    OpenMultiValued(singleParam,
                    singleParam.name,
                    singleParam.description,
                    default)

  case class OpenMultiValued[T, U <: Parameter[T]](singleParam: U,
                                                   name: Symbol,
                                                   description: Option[String],
                                                   default: Option[Seq[T]])
      extends MultiValued[T, U]
      with OpenParameter[Seq[T], ClosedMultiValued[T, U]] {

    override def closeWith(t: Seq[T]): ClosedMultiValued[T, U] =
      ClosedMultiValued(singleParam, name, description, default, t)
  }

  case class ClosedMultiValued[T, U <: Parameter[T]](
      singleParam: U,
      name: Symbol,
      description: Option[String],
      default: Option[Seq[T]],
      value: Seq[T])
      extends MultiValued[T, U]
      with ClosedParameter[Seq[T], ClosedMultiValued[T, U]]
}

sealed trait FormFieldParameter[T] extends Parameter[T]

object FormFieldParameter {

  def apply[T](name: Symbol,
               description: Option[String] = None,
               default: Option[T] = None,
               enum: Option[Seq[T]] = None): FormFieldParameter[T] =
    OpenFormFieldParameter(name, description, default, enum)

  def unapply[T](fp: FormFieldParameter[T]): Option[T] = fp match {
    case OpenFormFieldParameter(_, _, default, _)    => default
    case ClosedFormFieldParameter(_, _, _, _, value) => Some(value)
    case _                                           => None
  }

  case class OpenFormFieldParameter[T](name: Symbol,
                                       description: Option[String],
                                       default: Option[T],
                                       enum: Option[Seq[T]])
      extends FormFieldParameter[T]
      with OpenParameter[T, ClosedFormFieldParameter[T]] {

    override def closeWith(t: T): ClosedFormFieldParameter[T] =
      ClosedFormFieldParameter(name, description, default, enum, t)
  }

  case class ClosedFormFieldParameter[T](name: Symbol,
                                         description: Option[String],
                                         default: Option[T],
                                         enum: Option[Seq[T]],
                                         value: T)
      extends FormFieldParameter[T]
      with ClosedParameter[T, ClosedFormFieldParameter[T]]
}

sealed trait QueryParameter[T] extends Parameter[T]

object QueryParameter {

  def apply[T](name: Symbol,
               description: Option[String] = None,
               default: Option[T] = None,
               enum: Option[Seq[T]] = None): QueryParameter[T] =
    OpenQueryParameter(name, description, default, enum)

  def unapply[T](qp: QueryParameter[T]): Option[T] = qp match {
    case OpenQueryParameter(_, _, default, _)    => default
    case ClosedQueryParameter(_, _, _, _, value) => Some(value)
  }

  case class OpenQueryParameter[T](name: Symbol,
                                   description: Option[String],
                                   default: Option[T],
                                   enum: Option[Seq[T]])
      extends QueryParameter[T]
      with OpenParameter[T, ClosedQueryParameter[T]] {
    override def closeWith(t: T): ClosedQueryParameter[T] =
      ClosedQueryParameter(name, description, default, enum, t)
  }

  case class ClosedQueryParameter[T](name: Symbol,
                                     description: Option[String],
                                     default: Option[T],
                                     enum: Option[Seq[T]],
                                     value: T)
      extends QueryParameter[T]
      with ClosedParameter[T, ClosedQueryParameter[T]]

}

sealed trait PathParameter[T] extends Parameter[T]

object PathParameter {

  def apply[T](name: Symbol,
               description: Option[String] = None,
               default: Option[T] = None,
               enum: Option[Seq[T]] = None): PathParameter[T] =
    OpenPathParameter(name, description, default, enum)

  def unapply[T](pp: PathParameter[T]): Option[T] = pp match {
    case OpenPathParameter(_, _, default, _)    => default
    case ClosedPathParameter(_, _, _, _, value) => Some(value)
  }

  case class OpenPathParameter[T](name: Symbol,
                                  description: Option[String],
                                  default: Option[T],
                                  enum: Option[Seq[T]])
      extends PathParameter[T]
      with OpenParameter[T, ClosedPathParameter[T]] {

    override def closeWith(t: T): ClosedPathParameter[T] =
      ClosedPathParameter(name, description, default, enum, t)
  }

  case class ClosedPathParameter[T](name: Symbol,
                                    description: Option[String],
                                    default: Option[T],
                                    enum: Option[Seq[T]],
                                    value: T)
      extends PathParameter[T]
      with ClosedParameter[T, ClosedPathParameter[T]]

}

sealed trait BodyParameter[T] extends Parameter[T]

object BodyParameter {

  def apply[T](name: Symbol,
               description: Option[String] = None,
               default: Option[T] = None,
               enum: Option[Seq[T]] = None): BodyParameter[T] =
    OpenBodyParameter(name, description, default, enum)

  def unapply[T](bp: BodyParameter[T]): Option[T] = bp match {
    case OpenBodyParameter(_, _, default, _)    => default
    case ClosedBodyParameter(_, _, _, _, value) => Some(value)
  }

  case class OpenBodyParameter[T](name: Symbol,
                                  description: Option[String],
                                  default: Option[T],
                                  enum: Option[Seq[T]])
      extends BodyParameter[T]
      with OpenParameter[T, ClosedBodyParameter[T]] {

    override def closeWith(t: T): ClosedBodyParameter[T] =
      ClosedBodyParameter(name, description, default, enum, t)
  }

  case class ClosedBodyParameter[T](name: Symbol,
                                    description: Option[String],
                                    default: Option[T],
                                    enum: Option[Seq[T]],
                                    value: T)
      extends BodyParameter[T]
      with ClosedParameter[T, ClosedBodyParameter[T]]

}

sealed trait HeaderParameter[T] extends Parameter[T]

object HeaderParameter {

  def apply[T](name: Symbol,
               description: Option[String] = None,
               default: Option[T] = None,
               enum: Option[Seq[T]] = None): HeaderParameter[T] =
    OpenHeaderParameter(name, description, default, enum)

  def unapply[T](hp: HeaderParameter[T]): Option[T] = hp match {
    case OpenHeaderParameter(_, _, default, _)    => default
    case ClosedHeaderParameter(_, _, _, _, value) => Some(value)
  }

  case class OpenHeaderParameter[T](name: Symbol,
                                    description: Option[String],
                                    default: Option[T],
                                    enum: Option[Seq[T]])
      extends HeaderParameter[T]
      with OpenParameter[T, ClosedHeaderParameter[T]] {

    override def closeWith(t: T): ClosedHeaderParameter[T] =
      ClosedHeaderParameter(name, description, default, enum, t)
  }

  case class ClosedHeaderParameter[T](name: Symbol,
                                      description: Option[String],
                                      default: Option[T],
                                      enum: Option[Seq[T]],
                                      value: T)
      extends HeaderParameter[T]
      with ClosedParameter[T, ClosedHeaderParameter[T]]
}
