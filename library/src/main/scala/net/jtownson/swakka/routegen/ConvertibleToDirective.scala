package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.FormFieldDirectives.FieldMagnet._
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshal, _}
import akka.stream.ActorMaterializer
import net.jtownson.swakka.jsonschema.ApiModelDictionary._
import net.jtownson.swakka.model.Parameters.BodyParameter.OpenBodyParameter
import net.jtownson.swakka.model.Parameters.FormParameter1.OpenFormParameter1
import net.jtownson.swakka.model.Parameters.FormParameter2.OpenFormParameter2
import net.jtownson.swakka.model.Parameters.HeaderParameter.OpenHeaderParameter
import net.jtownson.swakka.model.Parameters.PathParameter.OpenPathParameter
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.model.Parameters._
import net.jtownson.swakka.routegen.PathHandling.{containsParamToken, pathWithParamMatcher}
import shapeless.{::, HList, HNil}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe.TypeTag
import scala.util.{Failure, Success, Try}

trait ConvertibleToDirective[T] {
  def convertToDirective(modelPath: String, t: T): Directive1[T]
}

object ConvertibleToDirective {

  val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""^(?i)(true|false)$""".r) flatMap (s => Some(s.toBoolean))

  val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string =>
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }

  def converter[T](t: T)(implicit ev: ConvertibleToDirective[T]): ConvertibleToDirective[T] = ev

  implicit val stringReqQueryConverter: ConvertibleToDirective[QueryParameter[String]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.?(default)).map(close(qp))
        case None => parameter(qp.name).map(close(qp))
      }
    })

  implicit val stringOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[String]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.?(default)).map(os => close(qp)(Some(os)))
        case _ => parameter(qp.name.?).map(close(qp))
      }
    })

  implicit val floatReqQueryConverter: ConvertibleToDirective[QueryParameter[Float]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Float].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Float]).map(close(qp))
      }
    })

  implicit val floatOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Float]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Float].?(default)).map(of => close(qp)(Some(of)))
        case _ => parameter(qp.name.as[Float].?).map(close(qp))
      }
    })

  implicit val doubleReqQueryConverter: ConvertibleToDirective[QueryParameter[Double]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Double].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Double]).map(close(qp))
      }
    })

  implicit val doubleOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Double]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Double].?(default)).map(dp => close(qp)(Some(dp)))
        case _ => parameter(qp.name.as[Double].?).map(close(qp))
      }
    })

  implicit val booleanReqQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Boolean].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Boolean]).map(close(qp))
      }
    })

  implicit val booleanOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Boolean]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Boolean].?(default)).map(bp => close(qp)(Some(bp)))
        case _ => parameter(qp.name.as[Boolean].?).map(close(qp))
      }
    })

  implicit val intReqQueryConverter: ConvertibleToDirective[QueryParameter[Int]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Int].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Int]).map(close(qp))
      }
    })

  implicit val intOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Int]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Int].?(default)).map(ip => close(qp)(Some(ip)))
        case _ => parameter(qp.name.as[Int].?).map(close(qp))
      }
    })

  implicit val longReqQueryConverter: ConvertibleToDirective[QueryParameter[Long]] =
    instance(qp => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Long].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Long]).map(close(qp))
      }
    })

  implicit val longOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Long]]] =
    instance(qp => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Long].?(default)).map(lp => close(qp)(Some(lp)))
        case _ => parameter(qp.name.as[Long].?).map(close(qp))
      }
    })


  implicit val stringReqPathConverter: ConvertibleToDirective[PathParameter[String]] =
    pathParamDirective(Segment)

  implicit val floatPathConverter: ConvertibleToDirective[PathParameter[Float]] =
    pathParamDirective(FloatNumber)

  implicit val doublePathConverter: ConvertibleToDirective[PathParameter[Double]] =
    pathParamDirective(DoubleNumber)

  implicit val booleanPathConverter: ConvertibleToDirective[PathParameter[Boolean]] =
    pathParamDirective(BooleanSegment)

  implicit val intPathConverter: ConvertibleToDirective[PathParameter[Int]] =
    pathParamDirective(IntNumber)

  implicit val longPathConverter: ConvertibleToDirective[PathParameter[Long]] =
    pathParamDirective(LongNumber)


  implicit val stringReqHeaderConverter: ConvertibleToDirective[HeaderParameter[String]] =
    requiredHeaderParamDirective(s => s)

  implicit val stringOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[String]]] =
    optionalHeaderParamDirective(s => s)

  implicit val floatReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Float]] =
    requiredHeaderParamDirective(_.toFloat)

  implicit val floatOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Float]]] =
    optionalHeaderParamDirective(_.toFloat)

  implicit val doubleReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Double]] =
    requiredHeaderParamDirective(_.toDouble)

  implicit val doubleOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Double]]] =
    optionalHeaderParamDirective(_.toDouble)

  implicit val booleanReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Boolean]] =
    requiredHeaderParamDirective(_.toBoolean)

  implicit val booleanOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Boolean]]] =
    optionalHeaderParamDirective(_.toBoolean)

  implicit val intReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Int]] =
    requiredHeaderParamDirective(_.toInt)

  implicit val intOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Int]]] =
    optionalHeaderParamDirective(_.toInt)

  implicit val longReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Long]] =
    requiredHeaderParamDirective(_.toLong)

  implicit val longOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Long]]] =
    optionalHeaderParamDirective(_.toLong)

  implicit def bodyParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[T]] =
    (_: String, bp: BodyParameter[T]) => {
      bp.default match {
        case None => entity(as[T]).map(close(bp))
        case Some(default) => optionalEntity[T](as[T]).map {
          case Some(value) => close(bp)(value)
          case None => close(bp)(default)
        }
      }
    }

  implicit def bodyOptParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[Option[T]]] =
    (_: String, bp: BodyParameter[Option[T]]) => {
      bp.default match {
        case None => optionalEntity[T](as[T]).map(close(bp))
        case Some(default) => optionalEntity[T](as[T]).map {
          case v@Some(_) => close(bp)(v)
          case None => close(bp)(default)
        }
      }
    }

  def optionalEntity[T](unmarshaller: FromRequestUnmarshaller[T]): Directive1[Option[T]] =
    entity(as[String]).flatMap { stringEntity =>
      if (stringEntity == null || stringEntity.isEmpty) {
        provide(Option.empty[T])
      } else {
        entity(unmarshaller).flatMap(e => provide(Some(e)))
      }
    }

  def formParamConverter1[P1 : FromStringUnmarshaller, T : TypeTag](constructor: (P1) => T): ConvertibleToDirective[FormParameter1[P1, T]] =
    (_: String, fp: FormParameter1[P1, T]) => {
      val fields: Seq[String] = apiModelKeys[T]

      formFields(Symbol(fields(0)).as[P1]).map(f => close(fp)(constructor.apply(f)))
    }


  sealed trait Defaulter[T] {
    def name: String
    def getWithDefault: T
  }

  implicit def nonOptionDefaulter[T]: Defaulter[T] = new Defaulter[T] {
    def name = "NonOptionDefaulter"
    override def getWithDefault = throw new IllegalStateException()
  }

  implicit def optionDefaulter[U]: Defaulter[Option[U]] = new Defaulter[Option[U]] {
    def name = "OptionDefaulter"
    override def getWithDefault = None
  }

  def formParamConverter2[P1: FromStringUnmarshaller, P2: FromStringUnmarshaller, T : TypeTag]
  (constructor: (P1, P2) => T)
  (implicit p1Default: Defaulter[P1], p2Default: Defaulter[P2], materializer: ActorMaterializer, ec: ExecutionContext):
  ConvertibleToDirective[FormParameter2[P1, P2, T]] =
    (_: String, fp: FormParameter2[P1, P2, T]) => {

      val dictionary = apiModelDictionary[T]
      val fields: Seq[String] = apiModelKeys[T]

      entity(as[FormData]).flatMap((formData: FormData) => {
        val of1: Option[String] = formData.fields.get(fields(0))
        val of2: Option[String] = formData.fields.get(fields(1))

        val xf: Future[Option[P1]] = swap(of1.map(f1 => Unmarshal(f1).to[P1]))
        val yf: Future[Option[P2]] = swap(of2.map(f2 => Unmarshal(f2).to[P2]))

        val v: Future[(Option[P1], Option[P2])] = for {
          x <- xf
          y <- yf
        } yield (x, y)

        onComplete(v).flatMap({
          case Success((Some(p1), Some(p2))) =>
            provide(close(fp)(constructor.apply(p1, p2)))
          case Success((None, Some(p2))) if !dictionary(fields(0)).required =>
            provide(close(fp)(constructor.apply(p1Default.getWithDefault, p2)))
          case Success((Some(p1), None)) if !dictionary(fields(1)).required =>
            provide(close(fp)(constructor.apply(p1, p2Default.getWithDefault)))
          case Success((None, None)) if !(dictionary(fields(0)).required && dictionary(fields(1)).required) =>
            provide(close(fp)(constructor.apply(p1Default.getWithDefault, p2Default.getWithDefault)))
          case Success((_, _)) =>
            reject(MissingFormFieldRejection("One of our fields is missing"))
          case Failure(_) => ???
            reject(MalformedFormFieldRejection("some field", "did not load"))
        })

//        val oo2: Option[Option[Future[(P1, P2)]]] = of1.map(f1 => {
//
//          val oo1: Option[Future[(P1, P2)]] = of2.map(f2 => {
//
//            val eventualP1: Future[P1] = Unmarshal(f1).to[P1]
//            val eventualP2: Future[P2] = Unmarshal(f2).to[P2]
//
//            val ps: Future[(P1, P2)] = for {
//              p1 <- eventualP1
//              p2 <- eventualP2
//            } yield (p1Default.getWithDefault(p1), p2Default.getWithDefault(p2))
//
//            ps
//          })
//          oo1
//        })
//        val os: Option[Future[(P1, P2)]] = oo2.flatten
//
//
//        os match {
//          case Some(future) =>
//            onComplete(future).flatMap({
//              case Success((p1, p2)) =>
//                provide(close(fp)(constructor.apply(p1, p2)))
//              case Failure(_) => reject(MalformedFormFieldRejection("some field", "did not load"))
//            })
//          case None =>
//            reject(MissingFormFieldRejection("One of our fields is missing"))
//        }
//        val ps: Future[(P1, P2)] = for {
//          f1: String <- of1
//          f2: String <- of2
//          p1 <- Unmarshal(f1).to[P1]
//          p2 <- Unmarshal(f2).to[P2]
//        } yield (p1, p2)
//
//        onComplete(ps).flatMap({
//          case Success((p1, p2)) => provide(close(fp)(constructor.apply(p1, p2)))
//          case Failure(x) => reject(MissingFormFieldRejection("One of our fields is missing"))
//        })
//        ps match {
//          case Some((p1, p2)) =>
//            provide(close(fp)(constructor.apply(p1, p2)))
//          case _ =>
//            reject(MissingFormFieldRejection("One of our fields is missing"))
//        }
////        val ll: List[Option[_]] = List(of1, of2)
////        val missingFields = ll.zipWithIndex.map({
////          case (of, i) => {
////            val fNotMissing = (dictionary(fields(i)).required && of.isDefined) || !dictionary(fields(i)).required
////            if (fNotMissing)
////              None
////            else
////              Some(fields(i))
////          }
////        }).filter(_.isDefined)
////
////        val p1: P1 = null.asInstanceOf[P1]
////        val p2: P2 = null.asInstanceOf[P2]
//
//        if (missingFields.isEmpty)
//          provide(close(fp)(constructor.apply(p1, p2)))
//        else
//          reject(MissingFormFieldRejection(missingFields.mkString(",")))
      })

//      formFields(fields(0).as[P1].?, fields(1).as[P2].?).tflatMap(
//        {
//          case (of1, of2) => {
//            val ll: List[Option[_]] = List(of1, of2)
//            val missingFields = ll.zipWithIndex.map({
//              case (of, i) => {
//                val fNotMissing = (dictionary(fields(i)).required && of.isDefined) || !dictionary(fields(i)).required
//                if (fNotMissing)
//                  None
//                else
//                  Some(fields(i))
//              }
//            }).filter(_.isDefined)
//
//            //
//            if (missingFields.isEmpty) {
//              val p11: P1 = if (dictionary(fields(0)).required || of1.isDefined) of1.get else Option(None): P1
//
//              val p1 = if (! dictionary(fields(0)).required && !of1.isDefined) None: P2 else of1.get
//              val p1: P1 = if (dictionary(fields(0)).required) of1.get else of1
//              val p2 = if (dictionary(fields(1)).required || of2.isDefined) of2.get else None
//
//              provide(close(fp)(constructor.apply(p1, p2)))
//            }
//            else {
//              reject(MissingFormFieldRejection(missingFields.mkString(",")))
//            }
//          }
//        }
//      )
    }

  private def swap[T](x: Option[Future[T]])(implicit ec: ExecutionContext): Future[Option[T]] =
    x match {
      case Some(f) => f.map(Some(_))
      case None    => Future.successful(None)
    }

  implicit val hNilConverter: ConvertibleToDirective[HNil] =
    (modelPath: String, _: HNil) => {
      if (containsParamToken(modelPath))
        pass.tmap[HNil](_ => HNil)
      else
        path(PathHandling.splittingPathMatcher(modelPath)).tmap[HNil](_ => HNil)
    }

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective[H],
                                             tail: ConvertibleToDirective[T]): ConvertibleToDirective[H :: T] =
    (modelPath: String, l: H :: T) => {
      val headDirective: Directive1[H] = head.convertToDirective(modelPath, l.head)
      val tailDirective: Directive1[T] = tail.convertToDirective(modelPath, l.tail)

      (headDirective & tailDirective).tmap((t: (H, T)) => t._1 :: t._2)
    }

  private def requiredHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[T]] = (_: String, hp: HeaderParameter[T]) => {
    hp.default match {
      case Some(default) => optionalHeaderValueByName(hp.name).map {
        case Some(header) => close(hp)(valueParser(header))
        case None => close(hp)(default)
      }
      case None => headerValueByName(hp.name).map(value => close(hp)(valueParser(value)))
    }
  }

  private def optionalHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[Option[T]]] = (_: String, hp: HeaderParameter[Option[T]]) => {

    hp.default match {
      case Some(default) =>
        optionalHeaderValueByName(hp.name).map {
          case Some(header) => close(hp)(Some(valueParser(header)))
          case None => close(hp)(default)
        }
      case None =>
        optionalHeaderValueByName(hp.name).map {
          case Some(value) => close(hp)(Some(valueParser(value)))
          case None => close(hp)(None)
        }
    }
  }


  private def instance[T](f: T => Directive1[T]): ConvertibleToDirective[T] =
    (_: String, t: T) => f(t)

  private def close[T](qp: QueryParameter[T]): T => QueryParameter[T] =
    t => qp.asInstanceOf[OpenQueryParameter[T]].closeWith(t)

  private def close[T](pp: PathParameter[T]): T => PathParameter[T] =
    t => pp.asInstanceOf[OpenPathParameter[T]].closeWith(t)

  private def close[T](bp: BodyParameter[T]): T => BodyParameter[T] =
    t =>
      bp.asInstanceOf[OpenBodyParameter[T]].closeWith(t)

  private def close[T](hp: HeaderParameter[T]): T => HeaderParameter[T] =
    t => hp.asInstanceOf[OpenHeaderParameter[T]].closeWith(t)

  private def close[P1, T](fp: FormParameter1[P1, T]): T => FormParameter1[P1, T] =
    t => fp.asInstanceOf[OpenFormParameter1[P1, T]].closeWith(t)

  private def close[P1, P2, T](fp: FormParameter2[P1, P2, T]): T => FormParameter2[P1, P2, T] =
    t => fp.asInstanceOf[OpenFormParameter2[P1, P2, T]].closeWith(t)

  private def pathParamDirective[T](pm: PathMatcher1[T]): ConvertibleToDirective[PathParameter[T]] = {
    (modelPath: String, pp: PathParameter[T]) =>
      rawPathPrefixTest(pathWithParamMatcher(modelPath, pp.name.name, pm)).map(close(pp))
  }
}
