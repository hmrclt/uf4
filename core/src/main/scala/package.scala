package ltbs

import cats.implicits._
import cats.{Monoid, Applicative, Monad}
import language.higherKinds

package object uniform {

  implicit class RichAppOps[F[_]: Applicative, A: Monoid](e: F[A]) {
    def emptyUnless(b: => Boolean): F[A] =
      if(b) e else Monoid[A].empty.pure[F]

    def when(b: => Boolean): F[Option[A]] =
      if(b) e.map{_.some} else none[A].pure[F]
  }

  implicit class RichMonadOps[F[_]: Monad,A](e: F[A]) {
    def emptyUnless(eb: F[Boolean])(implicit mon: Monoid[A]): F[A] = for {
      opt <- eb
      ret <- if (opt) e else mon.empty.pure[F]
    } yield ret

    def when(wmb: F[Boolean]): F[Option[A]] = for {
      opt <- wmb
      ret <- if (opt) e map {_.some} else none[A].pure[F]
    } yield ret
  }
}
