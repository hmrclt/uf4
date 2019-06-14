
import cats.{Applicative, Monoid, Monad}
import cats.implicits._
import ltbs.uniform._
import language.implicitConversions

sealed trait Tristate[A,B,C]
final case class TriLeft[A,B,C](a: A) extends Tristate[A,B,C]
final case class TriMid[A,B,C](b: B) extends Tristate[A,B,C]
final case class TriRight[A,B,C](c: C) extends Tristate[A,B,C]

case class AllTheTypes (
  aString: String,
  bigtext: BigString,
  aTriState: Tristate[String,Int,(Int,String)],
)

package object programs {

  type TellTypes = NilTypes
  type AskTypes = AllTheTypes :: BigString :: String :: (Int, Int) :: Int :: Boolean :: NilTypes

  def greasy[F[_] : Monad](
    interpreter: Language[F, TellTypes, AskTypes]
  ): F[Int] = {
    import interpreter._
    for {
      smalltext     <- ask[String]("smalltext")
      allTheTypes   <- ask[AllTheTypes]("allTheTypes")                
      bigtext       <- ask[BigString]("bigtext")
      age           <- ask[Int]("age")
      h2            <- ask[(Int,Int)]("height")
      height3       <- ask[Int]("height3")                  
      food          <- ask[Boolean]("wantFood")
      tea           <- ask[Boolean]("wantTea")
      baconCost     <- ask[Int]("bacon").map(_ * 12) emptyUnless food
      eggsCost      <- ask[Int]("eggs").map(_ * 24) emptyUnless food
      foodCost      = baconCost + eggsCost
      teaCost       <- ask[Int]("sugar").map(_ * 10 + 50) emptyUnless tea
      youngDiscount = if (age < 16) teaCost / 10 else 0
      oldDiscount   = if (age > 60) (teaCost + foodCost) * (Math.min(age - 60,25) / 100) else 0
    } yield (foodCost + teaCost + youngDiscount + oldDiscount)
  }


}
