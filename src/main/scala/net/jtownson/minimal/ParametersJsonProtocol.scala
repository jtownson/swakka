package net.jtownson.minimal

import net.jtownson.minimal.MinimalOpenApiModel.QueryParameter
import shapeless.{::, HList, HNil, Poly1}
import shapeless.ops.hlist._
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsNull, JsObject, JsString, JsValue, JsonFormat, JsonWriter}


object ParametersJsonProtocol extends DefaultJsonProtocol {

  val strParamWriter: JsonWriter[QueryParameter[String]] =
    qp => swaggerParam(qp.name, "query", "", false, JsString("string"))

  implicit val strParamFormat: JsonFormat[QueryParameter[String]] = lift(strParamWriter)

  val intParamWriter: JsonWriter[QueryParameter[Int]] =
    qp => swaggerParam(qp.name, "query", "", false, JsString("number"))


  implicit val intParamFormat: JsonFormat[QueryParameter[Int]] = lift(intParamWriter)

  val hNillParamWriter: JsonWriter[HNil] =
    _ => JsNull

  implicit val hNillParamFormat: JsonFormat[HNil] = lift(hNillParamWriter)


//  private def loop[H, T <: HList](l: H :: T)(implicit head: JsonFormat[H], tail: JsonFormat[T]): List[JsValue] = {
//    val h: H = l.head
//    val t: T = l.tail
//
//    if (t == HNil)
//      head.write(h) :: Nil
//    else {
//      val h2 :: t2 = t
//      head.write(h) :: loop(h2 :: t2)
//    }
//  }

//  def hConsParamWriter[H, T <: HList](implicit head: JsonFormat[H], tail: JsonFormat[T]): JsonWriter[H :: T] =
//    (l: H :: T) => JsObject("head" -> head.write(l.head), "tail" -> tail.write(l.tail))

  def hConsParamWriter[H, T <: HList](implicit head: JsonFormat[H], tail: JsonFormat[T]): JsonWriter[H :: T] =
  // i, j, l, m
  // [i, [j
    (l: H :: T) => {


      object mapIt extends Poly1 {
        implicit def caseJson[U : JsonFormat] = at[U](implicitly[JsonFormat[U]].write)
      }
      val v: HList = l.map(mapIt)
      println(v)
//      val h = l.head
//      val t = l.tail
//
//      if (t != HNil) {
//
//      } else {
//
//      }
//      val ll = List(head.write(l.head), tail.write(l.tail)).flatMap(List(_))
//      println(ll)
//      val v = tail.write(l.tail)
//      v.asJsObject
      JsArray(head.write(l.head))
    }

//  def hConsParamWriter[H, T <: HList](implicit head: JsonFormat[H], tail: JsonFormat[T]): JsonWriter[H :: T] = (l: H :: T) => {
//    val h :: t = l
//    val jsValues = loop(h :: t)
//    JsArray(jsValues: _*)
//  }


  implicit def hConsParamFormat[H, T <: HList](implicit head: JsonFormat[H], tail: JsonFormat[T]): JsonFormat[H :: T] = lift(hConsParamWriter[H, T])

  private def swaggerParam(name: Symbol, in: String, description: String, required: Boolean, `type`: JsValue): JsValue =
    JsObject(
      "name" -> JsString(name.toString()),
      "in" -> JsString(in),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "type" -> `type`
    )
}
