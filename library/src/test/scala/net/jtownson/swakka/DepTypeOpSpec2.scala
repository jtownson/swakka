package net.jtownson.swakka

import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.ops.hlist.Tupler
import shapeless.{::, HList, HNil}

// GOAL: Given a params list such as QueryParameter[String] :: QueryParameter[Int], pass and call a function f: (String, Int) => Unit (or some other return type).

// 2. Extract the inner types of the params hlist as a tuple
class DepTypeOpSpec2 extends FlatSpec {

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

  val f: (String, Int) => String = (s, i) => s"I got $s and $i"

  def munge[L, O <: HList, T](l: L)(implicit pv: ParameterValue.Aux[L, O], tv: Tupler.Aux[O, T]): T = tv(pv.get(l))

  "ParameterValue" should "Work" in {
    val l = p1 :: p2 :: HNil

    println(l.tupled)
    val r: (String, Int) = munge(l)

    println(f.tupled(r))
  }
}


