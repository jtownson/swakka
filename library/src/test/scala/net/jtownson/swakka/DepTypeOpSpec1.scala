package net.jtownson.swakka

import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.model.Parameters.{Parameter, QueryParameter}
import shapeless.{::, HList, HNil}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

// GOAL: Given a params list such as QueryParameter[String] :: QueryParameter[Int], pass and call a function f: (String, Int) => Unit (or some other return type).

// 1. Extract the inner types of the params hlist using dependent types
class DepTypeOpSpec1 extends FlatSpec {

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

    implicit def parameterValue[T]: Aux[QueryParameter[T], T] =
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

  val p1: QueryParameter[String] = OpenQueryParameter[String]('p1, None, None, None).closeWith("p1")
  val p2: QueryParameter[Int] = OpenQueryParameter[Int]('p2, None, None, None).closeWith(1)

  def munge[L, O](l: L)(implicit pv: ParameterValue.Aux[L, O]): O = pv.get(l)

  "ParameterValue" should "Work" in {
    val l = p1 :: p2 :: HNil
    val r = munge(l)
    println(r)
  }
}


