package ltbs.uniform
package interpreters

import cats.data._
import play.api.mvc.{ Request, Result, AnyContent }
import scala.concurrent.Future
import cats.syntax.eq._
import play.twirl.api.{Html => TwirlHtml}
import common.web._

package object playframework extends common.web.webcommon {

  type Encoded = String
  type WebInner[A] = RWST[Future, (JourneyConfig, List[String], Request[AnyContent]), Unit, (Path, DB), A]
  type WebMonad[A] = EitherT[WebInner, Result, A]

  type FormField[A,B] = common.web.FormField[A,B]

  implicit val tellTwirlUnit = new GenericWebTell[Unit,TwirlHtml] {
    def render(in: Unit):TwirlHtml = TwirlHtml("")
  }

  implicit val twirlUnitField = new FormField[Unit,TwirlHtml] {
    def decode(out: Input): Either[ltbs.uniform.ErrorTree,Unit] = Right(())
    def encode(in: Unit): Input = Input.empty
    def render(
      key: List[String],
      path: Path,
      data: Option[Input],
      errors: ErrorTree,
      messages: UniformMessages[TwirlHtml]
    ): TwirlHtml = TwirlHtml("")
  }


}
