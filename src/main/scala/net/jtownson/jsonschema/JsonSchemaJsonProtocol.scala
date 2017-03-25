//package net.jtownson.jsonschema
//
//import spray.json._
//
//object JsonSchemaJsonProtocol extends DefaultJsonProtocol {
//
//  import JsonSchemaTypes._
//
//  implicit val jsonTypeFormat = new RootJsonFormat[JsonType] {
//    override def write(jsonType: JsonType): JsValue = jsonType match {
//      case JsonNull => JsString("null")
//      case JsonObject => JsString("object")
//      case JsonBoolean => JsString("boolean")
//      case JsonArray => JsString("array")
//      case JsonNumber => JsString("number")
//      case JsonString => JsString("string")
//    }
//
//    override def read(json: JsValue): JsonType = json match {
//      case JsString("null") => JsonNull
//      case JsString("object") => JsonObject
//      case JsString("boolean") => JsonBoolean
//      case JsString("array") => JsonArray
//      case JsString("number") => JsonNumber
//      case JsString("string") => JsonString
//      case _ => throw DeserializationException(s"Illegal json type: $json")
//    }
//  }
//
////  implicit def booleanOrSchemaFormat[T] = new RootJsonFormat[BooleanOrSchema[T]] {
////    override def read(json: JsValue): BooleanOrSchema[T] = json match {
////      case JsBoolean(b) => Coproduct[BooleanOrSchema[T]](b)
////      case JsObject(_) => Coproduct[BooleanOrSchema[T]](jsonSchemaFormat.read(json))
////    }
////
////    override def write(obj: BooleanOrSchema[T]): JsValue = (obj.select[Boolean], obj.select[JsonSchema[T]]) match {
////      case (Some(b), _) => JsBoolean(b)
////      case (_, Some(schema)) => jsonSchemaFormat.write(schema)
////      case _ => throw DeserializationException(s"Illegal value: $obj")
////    }
////  }
//
////  implicit val seqSchemaFormat = lazyFormat(seqFormat[JsonSchema[_]])
//
////  implicit val seqBooleanSchemaFormat = seqFormat[BooleanOrSchema[_]] // TODO unused?
//
////  implicit val schemaOrSchemaArrayFormat: RootJsonFormat[SchemaOrSchemaArray[_]] =
////    new RootJsonFormat[SchemaOrSchemaArray[_]] {
////      override def read(json: JsValue): SchemaOrSchemaArray[_] = json match {
////        case JsArray(_) => Coproduct[SchemaOrSchemaArray[_]](seqSchemaFormat.read(json))
////        case JsObject(_) => Coproduct[SchemaOrSchemaArray[_]](jsonSchemaFormat.read(json))
////        case _ => throw DeserializationException(s"Unable to read json: $json")
////      }
////
////      override def write(obj: SchemaOrSchemaArray[_]): JsValue =
////        (obj.select[JsonSchema[_]], obj.select[Seq[JsonSchema[_]]]) match {
////          case (Some(schema: JsonSchema[_]), _) => jsonSchemaFormat.write(schema)
////          case (_, Some(schemaArray)) => seqFormat[JsonSchema[_]].write(schemaArray)
////          case _ => throw DeserializationException(s"Illegal value: $obj")
////        }
////    }
//
//  def baseSchemaFormat[T]: RootJsonFormat[JsonSchema[T]] = new RootJsonFormat[JsonSchema[T]] {
//    val fieldNames = List(
//      "id",
//      "$schema",
//      "title",
//      "description",
//      "multipleOf",
//      "maximum",
//      "exclusiveMaximum",
//      "minimum",
//      "exclusiveMinimum",
//      "maxLength",
//      "minLength",
//      "pattern",
//      "additionalItems",
//      "uniqueItems",
//      "$ref",
//      "required",
//      "additionalProperties",
//      "type",
//      "properties",
//      "patternProperties",
//      "items",
//      "allOf",
//      "anyOf")
//
//    val asJsString: PartialFunction[JsValue, String] = {case JsString(value) => value}
//    val asJsNumber: PartialFunction[JsValue, BigDecimal] = {case JsNumber(value) => value}
//    val asJsBoolean: PartialFunction[JsValue, Boolean] = {case JsBoolean(value) => value}
//
//    override def read(json: JsValue): JsonSchema[T] = {
//      val fields = json.asJsObject.fields
//      val fieldSeq: Seq[Option[JsValue]] = fieldNames.map(fields.get)
//      val (fields22, fieldsRemainder) = (fieldSeq.slice(0, 22), fieldSeq.slice(22, 24))
//
//      (fields22, fieldsRemainder) match {
//        case
//          (
//            Seq(
//            id,
//            schema,
//            title,
//            description,
//            multipleOf,
//            maximum,
//            exclusiveMaximum,
//            minimum,
//            exclusiveMinimum,
//            maxLength,
//            minLength,
//            pattern,
//            additionalItems,
//            uniqueItems,
//            ref,
//            required,
//            additionalProperties,
//            aType,
//            properties,
//            patternProperties,
//            items,
//            allOf),
//
//            Seq (anyOf, default)
//          ) =>
//
//
//          JsonSchema[T](
//            id = id map asJsString,
//            `$schema` = schema map asJsString,
//            title = title map asJsString,
//            description = description map asJsString,
//            multipleOf = multipleOf map asJsNumber,
//            maximum = maximum map asJsNumber,
//            exclusiveMaximum = exclusiveMaximum map asJsBoolean,
//            minimum = minimum map asJsNumber,
//            exclusiveMinimum = exclusiveMinimum map asJsBoolean,
//            maxLength = maxLength map asJsNumber,
//            minLength = minLength map asJsNumber,
//            pattern = pattern map asJsString,
//            //additionalItems map seqFormat[BooleanOrSchema[_]].read,
//            uniqueItems = uniqueItems map asJsBoolean,
//            `$ref` = ref map asJsString,
//            required = required map seqFormat[String].read,
//            //additionalProperties map booleanOrSchemaFormat.read,
//            `type` = aType map jsonTypeFormat.read,
////            properties map mapFormat[String, JsonSchema[_]].read,
////            patternProperties map mapFormat[String, JsonSchema[_]].read,
////            items map schemaOrSchemaArrayFormat.read,
////            allOf map seqSchemaFormat.read,
////            anyOf map seqSchemaFormat.read,
//          default = default map (/*_.convertTo[T]*/???)
//        )
//      }
//
//    }
//
//    override def write(obj: JsonSchema[T]): JsValue = {
//      val fields = List(
//      "id" -> obj.id.map(JsString(_)),
//      "$schema" -> obj.`$schema`.map(JsString(_)),
//      "title" -> obj.title.map(JsString(_)),
//      "description" -> obj.description.map(JsString(_)),
//      "multipleOf" -> obj.multipleOf.map(JsNumber(_)),
//      "maximum" -> obj.maximum.map(JsNumber(_)),
//      "exclusiveMaximum" -> obj.exclusiveMaximum.map(JsBoolean(_)),
//      "minimum" -> obj.minimum.map(JsNumber(_)),
//      "exclusiveMinimum" -> obj.exclusiveMinimum.map(JsBoolean(_)),
//      "maxLength" -> obj.maxLength.map(JsNumber(_)),
//      "minLength" -> obj.minLength.map(JsNumber(_)),
//      "pattern" -> obj.pattern.map(JsString(_)),
//      //"additionalItems" -> obj.additionalItems.map(seqBooleanSchemaFormat.write),
//      "uniqueItems" -> obj.uniqueItems.map(JsBoolean(_)),
//      "$ref" -> obj.`$ref`.map(JsString(_)),
//      "required" -> obj.required.map(seqFormat[String].write),
//      //"additionalProperties" -> obj.additionalProperties.map(booleanOrSchemaFormat.write),
//      "type" -> obj.`type`.map(jsonTypeFormat.write),
//      //"properties" -> obj.properties.map(mapFormat[String, JsonSchema[_]].write),
//      //"patternProperties" -> obj.patternProperties.map(mapFormat[String, JsonSchema[_]].write),
//      //"items" -> obj.items.map(schemaOrSchemaArrayFormat.write),
//      //"allOf" -> obj.allOf.map(seqFormat[JsonSchema[_]].write),
//      //"anyOf" -> obj.anyOf.map(seqFormat[JsonSchema[_]].write)
//        "default" -> obj.default.map(/*_.toJson*/???)
//      ).filter({ case (_, value) => value.isDefined })
//        .map({ case (key, value) => (key, value.get) })
//
//      JsObject(fields: _*)
//
//    }
//  }
//
//  implicit def jsonSchemaFormat[T]: RootJsonFormat[JsonSchema[T]] = rootFormat(lazyFormat(baseSchemaFormat))
//}
