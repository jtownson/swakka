package net.jtownson.swakka.jsonschema

import io.swagger.annotations.ApiModelProperty
import net.jtownson.swakka.misc.AnnotationExtractor.constructorAnnotations

import scala.reflect.runtime.universe._

// Produce additional schema documentation entries
// The resulting map is from the fields of a class
// to the ApiModelProperty entries for that field.

object ApiModelDictionary {

  implicit def apiModelDictionary[T: TypeTag]: Map[String, ApiModelPropertyEntry] =
      constructorAnnotations[T](classOf[ApiModelProperty]).map( kv => (kv._1, tuples2Property(kv._2)))

  private def tuples2Property(s: Set[(String, String)]): ApiModelPropertyEntry = {

    val value: String = s.find(findField("value")).getOrElse(("value", ""))._2
    val name: String = s.find(findField("name")).getOrElse("name", "")._2

    ApiModelPropertyEntry(value, name)
  }

  private def findField(field: String) = {
    (v: (String, String)) =>
      v match {
        case (f, _) if f == field => true
        case _ => false
      }
  }
}
