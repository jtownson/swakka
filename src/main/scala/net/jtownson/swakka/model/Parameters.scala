package net.jtownson.swakka.model

object Parameters {

  trait ClosedParameter[T] {
    def value: T
  }

  trait OpenParameter[T, U <: ClosedParameter[T]] {
    def value: T = throw new IllegalStateException(
      "The parameter is currently in the state of a template " +
        "without an associated value. Use closeWith to assign one.")

    def closeWith(t: T): U
  }

  sealed trait QueryParameter[T] {

    def value: T

    def name: Symbol

    def description: Option[String]

    def required: Boolean
  }

  object QueryParameter {

    def apply[T](name: Symbol, description: Option[String] = None, required: Boolean = false): QueryParameter[T] =
      OpenQueryParameter(name, description, required)

    case class OpenQueryParameter[T](name: Symbol, description: Option[String], required: Boolean)
      extends QueryParameter[T] with OpenParameter[T, ClosedQueryParameter[T]] {
      override def closeWith(t: T): ClosedQueryParameter[T] =
        ClosedQueryParameter(name, description, required, t)
    }

    case class ClosedQueryParameter[T](name: Symbol, description: Option[String], required: Boolean, value: T)
      extends QueryParameter[T] with ClosedParameter[T]

  }

  sealed trait PathParameter[T] {
    def name: Symbol

    def description: Option[String]

    def required: Boolean
  }

  object PathParameter {

    def apply[T](name: Symbol, description: Option[String] = None, required: Boolean = false): PathParameter[T] =
      OpenPathParameter(name, description, required)

    case class OpenPathParameter[T](name: Symbol, description: Option[String], required: Boolean)
      extends PathParameter[T] with OpenParameter[T, ClosedPathParameter[T]] {
      override def closeWith(t: T): ClosedPathParameter[T] =
        ClosedPathParameter(name, description, required, t)
    }

    case class ClosedPathParameter[T](name: Symbol, description: Option[String], required: Boolean, value: T)
      extends PathParameter[T] with ClosedParameter[T]

  }

  sealed trait BodyParameter[T] {
    def name: Symbol
  }

  object BodyParameter {

    def apply[T](name: Symbol): BodyParameter[T] =
      OpenBodyParameter(name)

    case class OpenBodyParameter[T](name: Symbol) extends BodyParameter[T] with OpenParameter[T, ClosedBodyParameter[T]] {
      override def closeWith(t: T): ClosedBodyParameter[T] =
        ClosedBodyParameter(name, t)
    }

    case class ClosedBodyParameter[T](name: Symbol, value: T) extends BodyParameter[T] with ClosedParameter[T]

  }

  sealed trait HeaderParameter[T] {
    def name: Symbol

    def description: Option[String]

    def required: Boolean
  }

  object HeaderParameter {

    def apply[T](name: Symbol, description: Option[String] = None, required: Boolean = false):
      HeaderParameter[T] = OpenHeaderParameter(name, description, required)

    case class OpenHeaderParameter[T](name: Symbol, description: Option[String], required: Boolean)
      extends HeaderParameter[T] with OpenParameter[T, ClosedHeaderParameter[T]] {
      override def closeWith(t: T): ClosedHeaderParameter[T] =
        ClosedHeaderParameter(name, description, required, t)
    }

    case class ClosedHeaderParameter[T](name: Symbol, description: Option[String], required: Boolean, value: T)
      extends HeaderParameter[T] with ClosedParameter[T]

  }

}