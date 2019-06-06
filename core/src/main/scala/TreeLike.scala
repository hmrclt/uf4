package ltbs.uniform

import simulacrum._
import scala.language.implicitConversions
import cats.data.{NonEmptyList => NEL}

@typeclass trait TreeLike[T] {

  type Key
  type Value

  def subTree(a: T, key: Key): T
  def /(a: T, key: Key): T = subTree(a,key)

  def valueAt(a: T, key: Key): Option[Value] =
    valueAtRoot(subTree(a, key))

  def valueAtPath(a: T, key: List[Key]): Option[Value] = {
    valueAtRoot(atPath(a, key))
  }

  def valueAtRoot(a: T): Option[Value]

  def definedAt(a: T, key: Key): Boolean =
    valueAt(a, key).isDefined

  def definedAtPath(a: T, key: List[Key]): Boolean =
    valueAtPath(a, key).isDefined

  def definedAtRoot(a: T): Boolean =
    valueAtRoot(a).isDefined

  def isEmpty(a: T): Boolean = a == empty
  def isNonEmpty(a: T): Boolean = !isEmpty(a)

  def empty: T

  def one(in: Value): T

  def atPath(a: T, path: List[Key]): T = {

    @annotation.tailrec
    def inner(a: T, path: List[Key]): T = {
      path match {
        case Nil => a
        case (x::xs) => inner(subTree(a,x), xs)
      }
    }
    inner(a, path)
  }

}

trait TreeLikeInstances {

  def mapTree[K,V]: TreeLike[Map[List[K],V]] = new MapTree[K,V]

  class MapTree[K,V] extends TreeLike[Map[List[K],V]] {
    type Key = K
    type Value = V
    type T = Map[List[K],V]

    def subTree(a: T, key: Key): T =
      a.collect { case (`key`::rem, v) =>
        (rem, v)
      }

    val empty: Map[List[K],V] = Map.empty
    def one(in: Value): Map[List[Key],Value] = Map(List.empty[Key] -> in)
    def valueAtRoot(a: Map[List[Key],Value]): Option[Value] = a.get(Nil)
  }

  implicit val treeLikeErrorTree = new TreeLike[ErrorTree] {

    type Key = String
    type Value = NEL[ErrorMsg]

    def subTree(a: ErrorTree, keyPath: String): ErrorTree = {
      a.flatMap { case (allPaths, errs) =>
        val refinedPaths = allPaths.collect {
          case (`keyPath`::rem) => rem
        }
        NEL.fromList(refinedPaths) match {
          case None => Nil
          case Some(p) => List(p -> errs)
        }
      }
    }

    val empty: ErrorTree = Map.empty
    def one(in: NEL[ErrorMsg]): ErrorTree = Map (
      NEL.one(Nil) -> in
    )

    def oneErr(in: ErrorMsg): ErrorTree = Map (
      NEL.one(Nil) -> NEL.one(in)
    )

    def valueAtRootList(a: ErrorTree): List[ErrorMsg] = valueAtRoot(a) match {
      case None => Nil
      case Some(nel) => nel.toList
    }

    def valueAtRoot(a: ErrorTree): Option[NEL[ErrorMsg]] = a.get(NEL.one(Nil))

    def simplified(a: ErrorTree): Map[InputPath, ErrorMsg] = a flatMap {
      case (paths, errors) =>
        errors.toList.map{ error =>
          (paths.head, error)
        }
    }

  }
}
