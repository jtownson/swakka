package net.jtownson.swakka.model

import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiModel._
import shapeless.ops.function.FnToProduct
import shapeless.{::, HList, HNil}

/**
  * Invoker provides the type plumbing to map the type of an input HList of Parameter[T]
  * to that of a function accepting an argument list of the inner types of those Parameters.
  * For eg. if the input HList is
  * QueryParameter[String] :: QueryParameter[Int] :: PathParameter[Boolean] :: HNil
  * Invoker will define a dependent Function type
  * (String, Int, Boolean) => R
  * and call that function, returning the result, R (also a dependent type).
  *
  * AkkHttpInvoker is a specialization of Invoker where the return type of F
  * is an akka Route.
  */
trait Invoker[Params] {
  type F
  type O

  def apply(f: F, l: Params): O
}

object Invoker {

  type Aux[L, FF, OO] = Invoker[L] {type F = FF; type O = OO}
  type AkkaHttpInvoker[L, F] = Invoker.Aux[L, F, Route]

  def apply[L](implicit invoker: Invoker[L]): Aux[L, invoker.F, invoker.O] = invoker

  implicit def invoker[Params, RawParams, FF, OO]
  (implicit
   pv: ParameterValue.Aux[Params, RawParams],
   fp: FnToProduct.Aux[FF, RawParams => OO]
  ): Invoker.Aux[Params, FF, OO] =
    new Invoker[Params] {
      type F = FF
      type O = OO

      override def apply(f: F, l: Params): O = {
        fp(f)(pv.get(l))
      }
    }
}


trait ParameterValue[P] {
  type Out

  def get(p: P): Out
}

object ParameterValue {
  type Aux[P, O] = ParameterValue[P] {type Out = O}

  def apply[P](implicit inst: ParameterValue[P]): Aux[P, inst.Out] = inst

  def instance[P, O](f: P => O): Aux[P, O] = new ParameterValue[P] {
    type Out = O

    override def get(p: P) = f(p)
  }

  implicit def queryParameterValue[T]: Aux[QueryParameter[T], T] =
    instance(p => p.value)

  implicit def pathParameterValue[T]: Aux[PathParameter[T], T] =
    instance(p => p.value)

  implicit def headerParameterValue[T]: Aux[HeaderParameter[T], T] =
    instance(p => p.value)

  implicit def formParameterValue[T]: Aux[FormFieldParameter[T], T] =
    instance(p => p.value)

  implicit def bodyParameterValue[T]: Aux[BodyParameter[T], T] =
    instance(p => p.value)

  implicit def multiParameterValue[T, U <: Parameter[T]]: Aux[MultiValued[T, U], Seq[T]] =
    instance(p => p.value)

  implicit val hNilParameterValue: Aux[HNil, HNil] =
    instance(_ => HNil)

  implicit def hListParameterValue[H, T <: HList, HO, TO <: HList](
                                                                    implicit
                                                                    ph: Aux[H, HO],
                                                                    pt: Aux[T, TO]): Aux[H :: T, HO :: TO] =
    instance[H :: T, HO :: TO] {
      case (h :: t) => ph.get(h) :: pt.get(t)
    }
}

