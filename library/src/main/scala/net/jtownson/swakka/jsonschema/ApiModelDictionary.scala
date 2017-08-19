package net.jtownson.swakka.jsonschema

import io.swagger.annotations.ApiModelProperty
import net.jtownson.swakka.misc.AnnotationExtractor.constructorAnnotations
import net.jtownson.swakka.misc.FieldnameExtractor.fieldNameTypes

import scala.collection.immutable.ListMap
import scala.reflect.runtime.universe._

// Produce additional schema documentation entries
// The resulting map is from the fields of a class
// to the ApiModelProperty entries for that field.

object ApiModelDictionary {

  def apiModelDictionary[T: TypeTag]: Map[String, ApiModelPropertyEntry] = {

    val annotationEntries: Map[String, ApiModelPropertyEntry] =
      constructorAnnotations[T](classOf[ApiModelProperty]).map(kv => (kv._1, tuples2Property(kv._2)))

    val allEntries: Seq[(String, ApiModelPropertyEntry)] =
      fieldNameTypes[T].map(
        fieldNameType => {
          val (fieldName, itsType)  = fieldNameType
          (fieldName, annotationEntries.getOrElse(fieldName, ApiModelPropertyEntry(None, None, isRequired(itsType))))
        })

    ListMap(allEntries: _*)
  }

  def apiModelKeys[T: TypeTag]: Seq[String] =
    apiModelDictionary[T].keys.toSeq

  private def isRequired(fieldType: String): Boolean = fieldType != "Option"

  private def tuples2Property(s: Set[(String, String)]): ApiModelPropertyEntry = {

    val value: Option[String] = s.find(findField("value")).map(_._2)
    val name: Option[String] = s.find(findField("name")).map(_._2)
    val required: String = s.find(findField("required")).map(_._2).getOrElse("false")

    ApiModelPropertyEntry(name, value, required.toBoolean)
  }

  private def findField(field: String): ((String, String)) => Boolean = {
    case (f, _) if f == field => true
    case _ => false
  }
}
