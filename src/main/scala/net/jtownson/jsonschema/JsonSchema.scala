//package net.jtownson.jsonschema
//
//object JsonSchemaTypes {
//
//  sealed trait JsonType
//
//  case object JsonNull extends JsonType
//
//  case object JsonObject extends JsonType
//
//  case object JsonBoolean extends JsonType
//
//  case object JsonArray extends JsonType
//
//  case object JsonNumber extends JsonType
//
//  case object JsonString extends JsonType
//
//}
//
//// A json schema describing a type T
//case class JsonSchema[T](
//                       id: Option[String] = None,
//                       `$schema`: Option[String] = None,
//                       title: Option[String] = None,
//                       description: Option[String] = None,
//                       multipleOf: Option[BigDecimal] = None,
//                       maximum: Option[BigDecimal] = None,
//                       exclusiveMaximum: Option[Boolean] = None,
//                       minimum: Option[BigDecimal] = None,
//                       exclusiveMinimum: Option[Boolean] = None,
//                       maxLength: Option[BigDecimal] = None,
//                       minLength: Option[BigDecimal] = None,
//                       pattern: Option[String] = None,
//                       //additionalItems: Option[Seq[BooleanOrSchema[_]]] = None,
//                       uniqueItems: Option[Boolean] = None,
//                       `$ref`: Option[String] = None,
//                       required: Option[Seq[String]] = None,
//                       //additionalProperties: Option[BooleanOrSchema[_]] = None,
//                       `type`: Option[JsonType] = None,
//                       //properties: Option[Map[String, JsonSchema[_]]] = None,
//                       //patternProperties: Option[Map[String, JsonSchema[_]]] = None,
//                       //items: Option[SchemaOrSchemaArray[_]] = None,
//                       //allOf: Option[Seq[JsonSchema[_]]] = None,
//                       //anyOf: Option[Seq[JsonSchema[_]]] = None,
//                       default: Option[T] = None
//                     )
//
