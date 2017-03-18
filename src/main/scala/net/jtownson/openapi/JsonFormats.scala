//package net.jtownson.openapi
//
//import net.jtownson.openapi.OpenApiModel.{Contact, Default, Delete, ExternalDocs, Get, Head, HttpMethod, HttpResponseCode, Info, Licence, OpenApi, Operation, Options, Parameters, Patch, PathItem, Post, Put, ResponseCode, ResponseValue}
//import spray.json.DefaultJsonProtocol._
//import spray.json._
//
//import scala.reflect.runtime.universe._
//
//object JsonFormats {
//
//  implicit val infoFormat: RootJsonFormat[Info] = jsonFormat6(Info)
//  implicit val contactFormat: RootJsonFormat[Contact] = jsonFormat3(Contact)
//  implicit val licenceFormat: RootJsonFormat[Licence] = jsonFormat2(Licence)
//  implicit val pathItemFormat: RootJsonFormat[PathItem] = jsonFormat1(PathItem)
//
//  implicit val httpMethodFormat = new RootJsonFormat[HttpMethod] {
//    override def write(method: HttpMethod): JsValue = method match {
//      case Get => JsString("get")
//      case Put => JsString("put")
//      case Post => JsString("post")
//      case Delete => JsString("delete")
//      case Options => JsString("options")
//      case Head => JsString("head")
//      case Patch => JsString("patch")
//      case Parameters => JsString("parameters")
//    }
//
//    override def read(json: JsValue): HttpMethod = json match {
//      case JsString(method) => method.toLowerCase match {
//        case "get" => Get
//        case "put" => Put
//        case "post" => Post
//        case "delete" => Delete
//        case "options" => Options
//        case "head" => Head
//        case "patch" => Patch
//        case "parameters" => Parameters
//        case invalid => throw new RuntimeException(s"Invalid JSON structure. Illegal value for HTTP method: $invalid")
//      }
//    }
//  }
//
//  implicit val operationFormat: RootJsonFormat[Operation] = jsonFormat11(Operation)
//  implicit val externalDocsFormat: RootJsonFormat[ExternalDocs] = jsonFormat2(ExternalDocs)
//
//  implicit def responseValueFormat[T]: RootJsonFormat[ResponseValue[T]] = new RootJsonFormat[ResponseValue[T]] {
//    override def read(json: JsValue): ResponseValue[T] = ???
////    json match {
////      case JsObject(fields) =>
////        fields.get("type") match {
////          case "string" => ResponseValue
////        }
////      case _ => ???
////    //ResponseValue
////    }
//
//    override def write(obj: ResponseValue[T]): JsValue = ???
//  }
//
//  //implicit val responseValueFormat: RootJsonFormat[ResponseValue] = jsonFormat3(ResponseValue)
//  implicit val responseCodeFormat: RootJsonFormat[ResponseCode] = new RootJsonFormat[ResponseCode] {
//    override def write(responseCode: ResponseCode): JsValue = responseCode match {
//      case Default => JsString("default")
//      case HttpResponseCode(httpStatus) => JsString(httpStatus.toString)
//    }
//
//    override def read(json: JsValue): ResponseCode = json match {
//      case JsString("default") => Default
//      case JsString(responseCode) => HttpResponseCode(responseCode.toInt)
//    }
//  }
//
//  // TODO either
//  // create version of autoschema for spray or
//  // convert play json to spray json or
//  // use a shapeless format
//  // TODO at this point also want to think about the problem of
//  // marshalling swagger json schema (refs) into case classes.
//  // Obviously the json will need some kind of type hint.
//  // How to best do this?
//  implicit val typeFormat: RootJsonFormat[Type] = new RootJsonFormat[Type] {
//    override def write(t: Type): JsValue = ???
//
//    override def read(json: JsValue): Type = ???
//  }
//
//  //implicit val responseValueFormat: RootJsonFormat[ResponseValue] = jsonFormat3(ResponseValue)
//  //implicit val openApiFormat: RootJsonFormat[OpenApi] = jsonFormat8(OpenApi)
//}
