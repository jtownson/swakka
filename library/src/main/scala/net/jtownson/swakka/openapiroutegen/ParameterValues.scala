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
package net.jtownson.swakka.openapiroutegen

import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._

trait ParameterValues {

//  implicit def queryParameterValue[T]: ParameterValue.Aux[QueryParameter[T], T] =
//    ParameterValue.instance(p => p.value)
//
//  implicit def pathParameterValue[T]: ParameterValue.Aux[PathParameter[T], T] =
//    ParameterValue.instance(p => p.value)
//
//  implicit def headerParameterValue[T]: ParameterValue.Aux[HeaderParameter[T], T] =
//    ParameterValue.instance(p => p.value)
//
//  implicit def formParameterValue[T]: ParameterValue.Aux[FormFieldParameter[T], T] =
//    ParameterValue.instance(p => p.value)
//
//  implicit def bodyParameterValue[T]: ParameterValue.Aux[BodyParameter[T], T] =
//    ParameterValue.instance(p => p.value)
//
//  implicit def multiParameterValue[T, U <: Parameter[T]]: ParameterValue.Aux[MultiValued[T, U], Seq[T]] =
//    ParameterValue.instance(p => p.value)
}

object ParameterValues extends ParameterValues
