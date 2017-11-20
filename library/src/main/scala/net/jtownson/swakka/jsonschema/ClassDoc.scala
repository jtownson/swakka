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
  * @tparam T The type for which documentation entries apply.
  */
trait ClassDoc[T] {

  def entries: Map[String, FieldDoc]
}

object ClassDoc {

  def entries[T](implicit ev: ClassDoc[T]): Map[String, FieldDoc] = ev.entries

  // Instances live in SwaggerAnnotationClassDoc
}