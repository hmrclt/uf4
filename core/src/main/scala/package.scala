package ltbs

import cats.implicits._
import cats.{Monoid, Applicative, Monad}
import cats.data.NonEmptyList
import language.higherKinds

package object uniform extends TreeLike.ToTreeLikeOps with TreeLikeInstances {

  type Path = List[String]
  type Input = Map[Path, List[String]]
  object Input extends MapTree[String, List[String]] {
    def fromUrlEncodedString(in: String): Either[ErrorTree,Input] = {
      val ungrouped: List[(String, String)] =
        in.split("&").toList
          .map{_.split("=").toList}
          .collect { case (k::v::Nil) => k -> v }

      ungrouped.groupBy(_._1).map{ case (k, comb) =>
        k.split("[.]").toList.dropWhile(_.isEmpty) -> comb.map {_._2}
      }.asRight
    }
  }

  implicit class RichInput(input: Input) {
    def toUrlEncodedString: String = {
      input
        .flatMap { case (k, vs)=>
          vs.map { v =>
            s"""${k.mkString(".")}=${java.net.URLEncoder.encode(v, "UTF-8")}"""
          }
        }
        .mkString("&")
    }
  }

  implicit val inputTree = Input
  type ErrorTree = Map[NonEmptyList[Path], NonEmptyList[ErrorMsg]]
  lazy val ErrorTree = treeLikeErrorTree

  type ::[H,T <: shapeless.HList] = shapeless.::[H,T]
  type NilTypes = Unit :: shapeless.HNil

  implicit class RichAppOps[F[_]: Applicative, A](e: F[A]) {
    def emptyUnless(b: => Boolean)(implicit mon: Monoid[A]): F[A] =
      if(b) e else Monoid[A].empty.pure[F]

    def emptyUnless(eb: F[Boolean])(implicit mon: Monoid[A], monad: Monad[F]): F[A] = for {
      opt <- eb
      ret <- if (opt) e else mon.empty.pure[F]
    } yield ret

    def when(b: => Boolean): F[Option[A]] =
      if(b) e.map{_.some} else none[A].pure[F]

    def when(wmb: F[Boolean])(implicit monad: Monad[F]): F[Option[A]] = for {
      opt <- wmb
      ret <- if (opt) e map {_.some} else none[A].pure[F]
    } yield ret
    
  }

  implicit class RichRuleListList[A](inner: List[List[Rule[A]]]) {
    def combined: Rule[A] = {
      inner match {
        case Nil => Rule.noop
        case x => x.map{_.combineAll}.reduce(_ andThen _)
      }
    }
  }

}