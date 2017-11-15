    package net.jtownson.swakka

    import org.scalatest.FlatSpec
    import org.scalatest.Matchers._
    import shapeless.ops.function._
    import shapeless.{HList, HNil, _}

    class TypeclassOfFunctionTypeSpec extends FlatSpec {

      // Here, we know the return type of F is constrained to be O
      // (because that's how the shapeless FnToProduct typeclass works)
      def invoke1[F, I <: HList, O](f: F, i: I)
                                   (implicit ftp: FnToProduct.Aux[F, I => O]): O = ftp(f)(i)

      // So let's try to express that by extracting the input type of F as FI
      def invoke2[FI, I <: HList, O](f: FI => O, i: I)
                                    (implicit ftp: FnToProduct.Aux[FI => O, I => O]): O = ftp(f)(i)

      "Invoke" should "work for a Function1" in {

        // Here's our function (Int) => String
        val f: (Int) => String = (i) => s"I got $i"

        val l = 1 :: HNil

        // this works
        val r1: String = invoke1(f, l)

        // So does this. (With evidence that the compiler sees the function parameter list (Int) as just Int
        val r2: String = invoke2[Int, Int::HNil, String](f, l)

        r1 shouldBe "I got 1"
        r2 shouldBe "I got 1"
      }

      "Invoke" should "work for a Function2 by viewing it as a function1 of tuple" in {

        // Here's our function (Int) => String
        val f: (Int) => String = (i) => s"I got $i"

        val l = 1 :: HNil

        // this works
        val r1: String = invoke1(f, l)

        // So does this. (With evidence that the compiler sees the function parameter list (Int) as just Int
        val r2: String = invoke2[Int, Int::HNil, String](f, l)

        r1 shouldBe "I got 1"
        r2 shouldBe "I got 1"
      }

      "Invoke" should "work for a Function2" in {

        // Here's our function (String, Int) => String
        val f: (String, Int) => String = (s, i) => s"I got $s and $i"

        val l = "s" :: 1 :: HNil

        // this works
        val r1: String = invoke1(f, l)

        // But this does not compile. There is no expansion for the type of FI
        // (String, Int) != the function Parameter list (String, Int)
//        val r2: String = invoke2(f, l)
        /*
        Error:(...) type mismatch;
        found   : (String, Int) => String
          required: ? => String
            val r1: String = invoke1(f, l)
        */

        r1 shouldBe "I got s and 1"
//        r2 shouldBe "I got s and 1"
      }
    }


