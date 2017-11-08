package net.jtownson.swakka

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.model.Parameters._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.ops.hlist.Tupler
import shapeless.{::, HList, HNil}

// Given an hlist such as Container[String] :: Container[Int], pass function, f: (String, Int) => some return type.

// Begin compatibility with endpoint implementation

class DepTypeOpSpec6 extends FlatSpec with RouteTest with TestFrameworkInterface {

//  case class Container[T](value: T)

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

  trait ResponseFunction[Tuple] {
    type Function
    type Return
    def apply(f: Function, t: Tuple): Return
  }

  object ResponseFunction {
    type Aux[T, F, R] = ResponseFunction[T] { type Function = F; type Return = R }

    def apply[T](implicit inst: ResponseFunction[T]): Aux[T, inst.Function, inst.Return] = inst

    implicit def tuple1[A,R]: Aux[A, (A) => R, R] = new ResponseFunction[A] {
      type Function = A => R
      type Return = R

      override def apply(f: Function, t: A): Return = f(t)
    }

    implicit def tuple2[A,B,R]: Aux[(A,B), (A, B) => R, R] = new ResponseFunction[(A, B)] {
      type Function = (A,B) => R
      type Return = R

      override def apply(f: (A, B) => R, t: (A, B)): Return = f.tupled(t)
    }
  }

  trait FullInvoker[L, F] {
    type R
    def apply(l: L, f: F): R
  }

  object FullInvoker {
    type Aux[L, F, RR] = FullInvoker[L, F] { type R = RR }

    def apply[L, F](implicit fi: FullInvoker[L, F]): Aux[L, F, fi.R] = fi

    implicit def i1[L, O <: HList, T, F, RR]
      (implicit pv: ParameterValue.Aux[L, O], tv: Tupler.Aux[O, T], rfv: ResponseFunction.Aux[T, F, RR]):
      Aux[L, F, RR] = new FullInvoker[L, F] {
        type R = RR

      override def apply(l: L, f: F): R = rfv.apply(f, tv(pv.get(l)))
    }
  }

  type EndpointInvoker[L, F] = FullInvoker.Aux[L, F, Route]

  def munge[L, F](l: L, f: F)(implicit fi: EndpointInvoker[L, F]): Route = fi(l, f)

  "ParameterValue" should "work for a simple tuple2" in {
    val f: (String, Int) => Route = (s, i) => Directives.complete(s"I got $s and $i")

    val l: QueryParameter[String] :: QueryParameter[Int] :: HNil =
      OpenQueryParameter[String]('p1, None, None, None).closeWith("p1") ::
        OpenQueryParameter[Int]('p2, None, None, None).closeWith(1) :: HNil

    val r: Route = munge(l, f)

    Get("http://example.com") ~> r ~> check {
      responseAs[String] shouldBe "I got p1 and 1"
    }
  }

  it should "work for hnil" in {
    val f: Unit => Route = _ => Directives.complete("foo")

    val l = HNil

    val r = munge[HNil, Unit=>Route](l, f)

    Get("http://example.com") ~> r ~> check {
      responseAs[String] shouldBe "foo"
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)

}


