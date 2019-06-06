package ltbs.uniform
package interpreters

import cats.data._
import play.api.mvc.{ Request, Result, AnyContent }
import scala.concurrent.Future
import cats.syntax.eq._
import play.twirl.api.{Html => TwirlHtml}

package object playframework {

  // New
  type Path = List[String]
  type JourneyConfig = String

  type Encoded = String

  type WebInner[A] = RWST[Future, (JourneyConfig, Path, Request[AnyContent]), Unit, (Path, DB), A]
  type WebMonad[A] = EitherT[WebInner, Result, A]

  type DB = Map[List[String],String]

  def relativePath(from: List[String], to: List[String]): String = {
    import cats.instances.string._
    val (rem, add) = removeCommon(from, to)
      (rem.drop(1).map{_ => ".."} ++ add).mkString("/")
  }

  @annotation.tailrec
  def removeCommon[B: cats.Eq](
    x: List[B],
    y: List[B]
  ): (List[B], List[B]) = (x,y) match {
    case (x::xs, y::ys) if x === y => removeCommon(xs, ys)
    case a => a
  }

  implicit def formToPlayAsk[A, Html](
    implicit field: FormField[A, Html]
  ): GenericPlayAsk[A, Html] = new SimpleForm(field)

  implicit val tellTwirlUnit = new GenericPlayTell[Unit,TwirlHtml] {
    def render(in: Unit):TwirlHtml = TwirlHtml("")
  }

  implicit val twirlUnitField = new FormField[Unit,TwirlHtml] {
    def decode(out: Input): Either[ltbs.uniform.ErrorTree,Unit] = Right(())
    def encode(in: Unit): Input = Input.empty
    def render(
      path: List[String],
      data: Option[Input],
      errors: ErrorTree,
      messages: UniformMessages[TwirlHtml]
    ): TwirlHtml = TwirlHtml("")
  }


}
