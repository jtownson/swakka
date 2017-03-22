//package net.jtownson.expt
//
//
//object Domain {
//  case class A(i: Int)
//  case class B(k: String, a: A)
//}
//
//object Meta {
//  import shapeless.HList
//
//  case class Schema[T](defaultValue: T, properties: HList)
//}
//
//import spray.json.DefaultJsonProtocol
//import spray.json._
//object DomainJsonProtocol extends DefaultJsonProtocol {
//  import Domain._
//  implicit val formatA: RootJsonFormat[A] = jsonFormat1(A)
//  implicit val formatB: RootJsonFormat[B] = jsonFormat2(B)
//}
//
//object MetaJsonProtocol extends DefaultJsonProtocol {
//  import Meta._
//  import DomainJsonProtocol._
//  import spray.json._
//  import fommil.sjs.FamilyFormats._
//  implicit def formatSchema[T: JsonFormat]: RootJsonFormat[Schema[T]] = rootFormat(lazyFormat(jsonFormat2(Schema[T])))
//}
//
//object App {
//  import shapeless.HNil
//  import Domain._
//  import Meta._
//
//  val sampleSchema = Schema[B](
//    defaultValue = B("key", A(1)),
//    properties = (String, String) :: (String, A) :: HNil
//  )
//
//}