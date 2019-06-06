package ltbs.uniform

import cats.implicits._

trait SplitEpimorphism[A,B,E] {
  def f(in: A): Either[E,B]
  def g(out: B): A

  def withCheck(fb: B => Either[E,B]) = {
    val old = this
    new SplitEpimorphism[A,B,E] {
      def f(in: A): Either[E,B] = old.f(in).flatMap(fb)
      def g(out: B): A = old.g(out)
    }
  }

}
