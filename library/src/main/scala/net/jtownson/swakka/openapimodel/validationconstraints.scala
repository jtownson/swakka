package net.jtownson.swakka.openapimodel

trait ValidationConstraints[T] {
  def enum: Option[Seq[T]]
  def const: Option[T]
}

case class Unconstrained[T]() extends ValidationConstraints[T] {
  override def enum: Option[Seq[T]] = None

  override def const: Option[T] = None
}

case class AnyValidationConstraints[T](enum: Option[Seq[T]], const: Option[T])
    extends ValidationConstraints[T]

case class NumericValidationConstraints[T: Numeric](
    enum: Option[Seq[T]] = None,
    const: Option[T] = None,
    multipleOf: Option[T] = None,
    maximum: Option[T] = None,
    exclusiveMaximum: Option[T] = None,
    minimum: Option[T] = None,
    exclusiveMinimum: Option[T] = None)
    extends ValidationConstraints[T]

case class StringValidationConstraints(enum: Option[Seq[String]],
                                       const: Option[String],
                                       minLength: Option[Int],
                                       maxLength: Option[Long],
                                       pattern: Option[String])
    extends ValidationConstraints[String]

case class ArrayValidationConstraints[T, U <: Seq[T]](
    items: Option[Seq[T]],
    minItems: Option[Int],
    maxItems: Option[Long],
    uniqueItems: Option[Boolean],
    contains: Option[Seq[T]])
