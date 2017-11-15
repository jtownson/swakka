package net.jtownson.swakka

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.model.Parameters._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.ops.function.FnToProduct
import shapeless.{::, HList, HNil}

// Given an hlist such as Container[String] :: Container[Int], pass function, f: (String, Int) => some return type.

// Get rid of the second, F, type parameter at the call site.

class DepTypeOpSpec7 extends FlatSpec with RouteTest with TestFrameworkInterface {

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

    implicit def hListParameterValue[H, T <: HList, HO, TO <: HList]
      (implicit
       ph: Aux[H, HO],
       pt: Aux[T, TO]): Aux[H :: T, HO :: TO] =
      instance[H :: T, HO :: TO] {
        case (h :: t) => ph.get(h) :: pt.get(t)
      }
  }

  trait Invoker[LiftedParams] {
    type F
    type O
    def apply(f: F, l: LiftedParams): O
  }

  object Invoker {
    type Aux[L, FF, OO] = Invoker[L] { type F = FF; type O = OO }

    def apply[L](implicit invoker: Invoker[L]): Aux[L, invoker.F, invoker.O] = invoker

    implicit def invoker[LiftedParams, RawParams, FF, OO]
      (implicit
       pv: ParameterValue.Aux[LiftedParams, RawParams],
       fp: FnToProduct.Aux[FF, RawParams => OO]
      ): Invoker.Aux[LiftedParams, FF, OO] =
      new Invoker[LiftedParams] {
        type F = FF
        type O = OO

        override def apply(f: F, l: LiftedParams): O = {
          fp(f)(pv.get(l))
        }
      }
  }

  type AkkaHttpInvoker[L, F] = Invoker.Aux[L, F, Route]

  def munge[L, F](f: F, l: L)(implicit ainv: AkkaHttpInvoker[L, F]): Route =
    ainv(f, l)


  "ParameterValue" should "work for a simple tuple2" in {
    val f: (String, Int) => Route = (s, i) => Directives.complete(s"I got $s and $i")

    val l: QueryParameter[String] :: QueryParameter[Int] :: HNil =
        OpenQueryParameter[String]('p1, None, None, None).closeWith("p1") ::
        OpenQueryParameter[Int]('p2, None, None, None).closeWith(1) :: HNil

    val r: Route = munge(f, l)

    Get("http://example.com") ~> r ~> check {
      responseAs[String] shouldBe "I got p1 and 1"
    }
  }

//  it should "work for hnil" in {
//    val f: () => Route = () => Directives.complete("foo")
//
//    val l = HNil
//
//    val r = munge[HNil, HNil, () => Route](f, l)
//
//    Get("http://example.com") ~> r ~> check {
//      responseAs[String] shouldBe "foo"
//    }
//  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)

}


