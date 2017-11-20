package net.jtownson.swakka.jsonschema

/**
  * Typeclass for documenting case classes.
  * Instances should return an entries Map with
  * the keys being fieldnames in the case class.
  * Values are then the doc attached to that field.
  * e.g.
  * <code>
  *   case class A(i: Int)
  *
  *   val entries = Map("i" -> ApiModelPropertyEntry("something about i"))
  * </code>
  *
  * @tparam T The type for which documentation entries apply.
  */
trait ClassDoc[T] {

  def entries: Map[String, FieldDoc]
}

object ClassDoc {

  // Create a hardcoded ClassDoc instance for a class.
  def apply[T](tDocs: Map[String, FieldDoc]): ClassDoc[T] = new ClassDoc[T] {
    override def entries: Map[String, FieldDoc] = tDocs
  }

  def entries[T](implicit ev: ClassDoc[T]): Map[String, FieldDoc] = ev.entries

  // Instances
  import SwaggerAnnotationClassDoc._
  import scala.reflect.runtime.universe._

  implicit def apiDoc[T: TypeTag]: ClassDoc[T] = new ClassDoc[T] {
    override def entries: Map[String, FieldDoc] = annotationEntries[T]
  }
}