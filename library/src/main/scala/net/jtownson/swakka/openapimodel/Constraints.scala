package net.jtownson.swakka.openapimodel

case class Constraints[T](enum: Option[Set[T]] = None,
                          multipleOf: Option[T] = None,
                          maximum: Option[T] = None,
                          exclusiveMaximum: Option[T] = None,
                          minimum: Option[T] = None,
                          exclusiveMinimum: Option[T] = None,
                          minLength: Option[Int] = None,
                          maxLength: Option[Long] = None,
                          pattern: Option[String] = None,
                          items: Option[Seq[T]] = None,
                          minItems: Option[Int] = None,
                          maxItems: Option[Long] = None,
                          uniqueItems: Option[Boolean] = None,
                          contains: Option[Seq[T]] = None)
