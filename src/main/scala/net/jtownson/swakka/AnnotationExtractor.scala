package net.jtownson.swakka

import scala.reflect.runtime.universe._

// Who says scala reduces boilerplate ?!!?
object AnnotationExtractor {

  def constructorAnnotations[T: TypeTag](annotationClass: Class[_]): Map[String, Seq[(String, String)]] = {
    constructorAnnotationsRaw(annotationClass).
      groupBy(_._1).
      map( (t: (String, Seq[(String, String, String)])) => (t._1, drop1(t._2)))
  }

  private def drop1(s: Seq[(String, String, String)]): Seq[(String, String)] = {
    s.map(sss => (sss._2, sss._3))
  }

  private def constructorAnnotationsRaw[T: TypeTag](annotationClass: Class[_]): Seq[(String, String, String)] = {

    val tpe = typeOf[T]

    val constructor: MethodSymbol = primaryConstructor(tpe.decl(termNames.CONSTRUCTOR))

    val params: Seq[Symbol] = constructor.paramLists.flatten

    val annotatedParams = params.flatMap(param => tuple(param, annotationClass.getName))

    annotatedParams.flatMap(annotatedParam => {

      val (field, annotation) = annotatedParam

      val annotationValues: Seq[Tree] = annotation.tree.children.tail

      annotationValues.flatMap({
        case AssignOrNamedArg(Ident(name), Literal(value)) => {
          Some((field, name.decodedName.toString, value.value.toString))
        }
        case _ => None
      })
    })
  }

  private def tuple(s: Symbol, annotationName: String): Option[(String, Annotation)] = {
    swaggerAnnotation(s.annotations, annotationName).map(annotation => (s.name.toString, annotation))
  }

  private def swaggerAnnotation(annotations: Seq[Annotation], annotationName: String): Option[Annotation] = {
    annotations.flatMap(annotation => annotation.tree.tpe.typeSymbol.fullName match {
      case s if s == annotationName => Some(annotation)
      case _ => None
    })
    annotations.headOption
  }

  private def primaryConstructor[T: TypeTag](constructorSymbol: Symbol) = {
    if (constructorSymbol.isMethod) {
      constructorSymbol.asMethod
    }
    else {
      val constructors = constructorSymbol.asTerm.alternatives
      constructors.map(_.asMethod).find(_.isPrimaryConstructor).get
    }
  }
}
