package ltbs.uniform

import cats.implicits._
import cats.Monoid
import cats.data.NonEmptyList

case class ErrorMsg(msg: String, args: Any*) {
  def render[A](msgProvider: UniformMessages[A]): A =
    msgProvider(msg, args:_*)

  def toTree: ErrorTree = {
    ErrorTree.one(NonEmptyList.one(this))
  }
}

trait Rule[A] {

  def apply(in: A): ErrorTree

  def either(in: A): Either[ErrorTree, A] = apply(in) match {
    case ErrorTree.empty => Right(in)
    case x => Left(x)
  }
  
  def andThen(that: Rule[A]): Rule[A] = {
    val orig = this
    new Rule[A] {
      def apply(in: A): ErrorTree = orig.apply(in) match {
        case ErrorTree.empty => that.apply(in)
        case xs  => xs
      }
    }
  }
}

object Rule {

  def assert[A](pred: A => Boolean)(error: ErrorMsg, pathH: Path, pathT: Path*) = {
    val paths = NonEmptyList(pathH,pathT.toList)
    new Rule[A] {
      def apply(in: A) = if (pred(in))
        ErrorTree.empty
      else
        Map(paths -> NonEmptyList.one(error))
    }
  }

  def apply[A](rules: (A => Boolean, (ErrorMsg, NonEmptyList[Path]))*): List[List[Rule[A]]] = 
    List(rules.toList.map{case (r, msg) => fromPred(r, msg)})

  def fromPred[A](pred: A => Boolean, msg: (ErrorMsg, NonEmptyList[Path])): Rule[A] = new Rule[A] {
    def apply(in: A) = if (pred(in)) ErrorTree.empty else Map(msg._2 -> NonEmptyList.one(msg._1))
  }

  implicit def vrMonoid[A]: Monoid[Rule[A]] = new Monoid[Rule[A]] {
    def empty: Rule[A] = noop
    def combine(x: Rule[A], y: Rule[A]) = new Rule[A] {
      def apply(in: A): ErrorTree = x.apply(in) |+| y.apply(in)
    }
  }

  def alwaysFail[A]: Rule[A] = new Rule[A] {
    def apply(in: A): ErrorTree = ErrorTree.oneErr(ErrorMsg("none-shall-pass"))
  }

  def noop[A]: Rule[A] = new Rule[A] {
    def apply(in: A): ErrorTree = ErrorTree.empty
  }

}
