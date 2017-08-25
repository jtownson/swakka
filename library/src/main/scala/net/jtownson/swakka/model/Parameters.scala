package net.jtownson.swakka.model

object Parameters {

  sealed trait Parameter[T] {
    def name: Symbol

    def description: Option[String]

    def value: T

    def default: Option[T]
  }

  sealed trait ClosedParameter[T, U] extends Parameter[T]

  sealed trait OpenParameter[T, U] extends Parameter[T] {
    def value: T = throw new IllegalStateException(
      "The parameter is currently in the state of a template " +
      "without an associated value. Parameters with values " +
      "can only be passing http requests through matching route structures. " +
      "(i.e. create an API definition, get its Route then make a request).")

    def closeWith(t: T): U
  }

  sealed trait FormParameter[+P, T] extends Parameter[T]

  object FormParameter {

    def apply[P, T](name: Symbol, description: Option[String] = None, default: Option[T] = None,
                                construct: P => T): FormParameter[P, T] =
      OpenFormParameter(name, description, default, construct)

    def unapply[P, T](fp: FormParameter[P, T]): Option[T] = fp match {
      case OpenFormParameter(_, _, default, _) => default
      case ClosedFormParameter(_, _, _, _, value) => Some(value)
      case _ => None
    }

    case class OpenFormParameter[P, T](
                                                     name: Symbol,
                                                     description: Option[String],
                                                     default: Option[T],
                                                     construct: P => T)
      extends FormParameter[P, T] with OpenParameter[T, ClosedFormParameter[P, T]] {

      override def closeWith(t: T): ClosedFormParameter[P, T] =
        ClosedFormParameter(name, description, default, construct, t)
    }

    case class ClosedFormParameter[P, T](
                                                       name: Symbol,
                                                       description: Option[String],
                                                       default: Option[T],
                                                       construct: (P) => T,
                                                       value: T)
      extends FormParameter[P, T] with ClosedParameter[T, ClosedFormParameter[P, T]]
  }


  sealed trait QueryParameter[T] extends Parameter[T]

  object QueryParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None): QueryParameter[T] =
        OpenQueryParameter(name, description, default)

    def unapply[T](qp: QueryParameter[T]): Option[T] = qp match {
      case OpenQueryParameter(_, _, default) => default
      case ClosedQueryParameter(_, _, _, value) => Some(value)
    }

    case class OpenQueryParameter[T](name: Symbol, description: Option[String],
                                     default: Option[T])
      extends QueryParameter[T] with OpenParameter[T, ClosedQueryParameter[T]] {
      override def closeWith(t: T): ClosedQueryParameter[T] =
        ClosedQueryParameter(name, description, default, t)
    }

    case class ClosedQueryParameter[T](name: Symbol, description: Option[String],
                                       default: Option[T], value: T)
      extends QueryParameter[T] with ClosedParameter[T, ClosedQueryParameter[T]]

  }

  sealed trait PathParameter[T] extends Parameter[T]

  object PathParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None): PathParameter[T] =
      OpenPathParameter(name, description, default)

    def unapply[T](pp: PathParameter[T]): Option[T] = pp match {
      case OpenPathParameter(_, _, default) => default
      case ClosedPathParameter(_, _, _, value) => Some(value)
    }

    case class OpenPathParameter[T](name: Symbol, description: Option[String],
                                    default: Option[T])
      extends PathParameter[T] with OpenParameter[T, ClosedPathParameter[T]] {

      override def closeWith(t: T): ClosedPathParameter[T] =
        ClosedPathParameter(name, description, default, t)
    }

    case class ClosedPathParameter[T](name: Symbol, description: Option[String],
                                      default: Option[T], value: T)
      extends PathParameter[T] with ClosedParameter[T, ClosedPathParameter[T]]

  }

  sealed trait BodyParameter[T] extends Parameter[T]

  object BodyParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None): BodyParameter[T] =
      OpenBodyParameter(name, description, default)

    def unapply[T](bp: BodyParameter[T]): Option[T] = bp match {
      case OpenBodyParameter(_, _, default) => default
      case ClosedBodyParameter(_, _, _, value) => Some(value)
    }

    case class OpenBodyParameter[T](name: Symbol, description: Option[String],
                                    default: Option[T])
      extends BodyParameter[T] with OpenParameter[T, ClosedBodyParameter[T]] {

      override def closeWith(t: T): ClosedBodyParameter[T] =
        ClosedBodyParameter(name, description, default, t)
    }

    case class ClosedBodyParameter[T](name: Symbol, description: Option[String],
                                      default: Option[T], value: T)
      extends BodyParameter[T] with ClosedParameter[T, ClosedBodyParameter[T]]

  }

  sealed trait HeaderParameter[T] extends Parameter[T]

  object HeaderParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None):
      HeaderParameter[T] = OpenHeaderParameter(name, description, default)

    def unapply[T](hp: HeaderParameter[T]): Option[T] = hp match {
      case OpenHeaderParameter(_, _, default) => default
      case ClosedHeaderParameter(_, _, _, value) => Some(value)
    }

    case class OpenHeaderParameter[T](name: Symbol, description: Option[String],
                                      default: Option[T])
      extends HeaderParameter[T] with OpenParameter[T, ClosedHeaderParameter[T]] {

      override def closeWith(t: T): ClosedHeaderParameter[T] =
        ClosedHeaderParameter(name, description, default, t)
    }

    case class ClosedHeaderParameter[T](name: Symbol, description: Option[String],
                                        default: Option[T], value: T)
      extends HeaderParameter[T] with ClosedParameter[T, ClosedHeaderParameter[T]]
  }
}