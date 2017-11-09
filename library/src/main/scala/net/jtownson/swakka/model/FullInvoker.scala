package net.jtownson.swakka.model

import akka.http.scaladsl.server.Route
import net.jtownson.swakka.model.Parameters._
import shapeless.{::, HList, HNil}
import shapeless.ops.hlist.Tupler

/**
  * FullInvoker provides the type plumbing to map the type of an input HList of Parameter[T]
  * to that of a function accepting an argument list of the inner types of those Parameters.
  * For eg. if the input HList is
  * QueryParameter[String] :: QueryParameter[Int] :: PathParameter[Boolean] :: HNil
  * full invoker will define a dependent Function type
  * (String, Int, Boolean) => R
  * and call that function, returning the result, R (also a dependent type).
  *
  * EndpointInvoker is a specialization of FullInvoker where the return type of F
  * is an akka Route.
  */
trait FullInvoker[L, F] {
  type R
  def apply(l: L, f: F): R
}

object FullInvoker {

  type EndpointInvoker[L, F] = FullInvoker.Aux[L, F, Route]

  trait ParameterValue[P] {
    type Out
    def get(p: P): Out
  }

  object ParameterValue {
    type Aux[P, O] = ParameterValue[P] { type Out = O }

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

  trait TupledFunction[Tuple] {
    type Function
    type Return
    def apply(f: Function, t: Tuple): Return
  }

  object TupledFunction {
    type Aux[T, F, R] = TupledFunction[T] { type Function = F; type Return = R }

    def apply[T](implicit inst: TupledFunction[T]): Aux[T, inst.Function, inst.Return] = inst

    implicit def tuple1[A,R]: Aux[A, (A) => R, R] = new TupledFunction[A] {
      type Function = A => R
      type Return = R

      override def apply(f: Function, t: A): Return = f(t)
    }

    implicit def tuple2[A,B,R]: Aux[(A,B), (A, B) => R, R] = new TupledFunction[(A, B)] {
      type Function = (A,B) => R
      type Return = R

      override def apply(f: (A, B) => R, t: (A, B)): Return = f.tupled(t)
    }
  }

  type Aux[L, F, RR] = FullInvoker[L, F] { type R = RR }

  def apply[L, F](implicit fi: FullInvoker[L, F]): Aux[L, F, fi.R] = fi

  implicit def fullInvoker[L, O <: HList, T, F, RR]
  (implicit pv: ParameterValue.Aux[L, O], tupler: Tupler.Aux[O, T], tupledFunction: TupledFunction.Aux[T, F, RR]):
  Aux[L, F, RR] = new FullInvoker[L, F] {
    type R = RR

    override def apply(l: L, f: F): R = tupledFunction(f, tupler(pv.get(l)))
  }
}
