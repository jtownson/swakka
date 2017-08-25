package net.jtownson.swakka.routegen

trait Tuplers {

  implicit def tupler[P1, T](f: (P1) => T): Tuple1[P1] => T =
    t => f(t._1)

  implicit def tupler[P1, P2, T](f: (P1, P2) => T): ((P1, P2)) => T =
    f.tupled

  implicit def untupler[P1, P2, T](f: ((P1, P2)) => T): (P1, P2) => T =
    (p1, p2) => f(Tuple2(p1, p2))

}

object Tuplers extends Tuplers
