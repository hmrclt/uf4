package ltbs.uniform
package interpreters.playframework

import cats.implicits._
import cats.data._
import play.api._,mvc._,http.Writeable
import reflect.runtime.universe.WeakTypeTag
import shapeless._, ops.hlist.Selector
import concurrent.{Future, ExecutionContext}

trait GenericPlayAsk[A,Html] {

  def page(
    targetId: String,
    currentId: String,
    tell: Html,
    default: Option[A],
    validation: List[List[ValidationRule[A]]],
    config: JourneyConfig,
    request: Request[AnyContent],
    path: List[String],
    db: DB,
    messages: UniformMessages[Html]
  ): Future[PageOut]

  case class PageOut(
    path: List[String],
    db: DB,
    output: Either[Result,A]
  ) {
    def toTransformerFormat: (Unit, (Path, DB), Either[Result, A]) =
      ((), (path, db), output)
  }

  def apply(
    targetId: String,
    tell: Html,
    default: Option[A],
    validation: List[List[ValidationRule[A]]],
    messages: Request[AnyContent] => UniformMessages[Html]
  )(implicit ec: ExecutionContext): WebMonad[A] = EitherT[WebInner, Result, A] {
    RWST { case ((config, currentId, request), (path, db)) =>
      page(
        targetId,
        currentId,
        tell,
        default,
        validation,
        config,
        request,
        path,
        db,
        messages(request)
      ).map{_.toTransformerFormat}
    }
  }

}

trait GenericPlayTell[A,Html] {
  def render(in: A): Html
}

abstract class PlayInterpreterUF4[Html: Writeable](
  implicit ec: ExecutionContext
) extends Compatibility.PlayController {

  type PlayAsk[A] = GenericPlayAsk[A, Html]
  type PlayTell[A] = GenericPlayTell[A, Html]

  def messages(
    request: Request[AnyContent],
    customContent: Map[String,(String,List[Any])]
  ): UniformMessages[Html]

  def renderForm(
    key: List[String],
    errors: ErrorTree,
    form: Html,
    breadcrumbs: List[String],
    request: Request[AnyContent],
    messages: UniformMessages[Html]
  ): Html

  val log: Logger = Logger("uniform")

  class FuturePlayInterpreter[
    SupportedTell <: HList,
    SupportedAsk  <: HList
  ](implicit
    tellSummoner : Summoner[SupportedTell, PlayTell],
    askSummoner  : Summoner[SupportedAsk, PlayAsk]
  ) extends Language[WebMonad, SupportedTell, SupportedAsk] {
    override def interact[Tell: WeakTypeTag, Ask: WeakTypeTag](
      id: String,
      t: Tell,
      default: Option[Ask],
      validation: List[List[ValidationRule[Ask]]],
      customContent: Map[String,(String,List[Any])]
    )(
      implicit selectorTell : Selector[SupportedTell, Tell],
      selectorAsk : Selector[SupportedAsk, Ask]
    ): WebMonad[Ask] = {
      val tellHtml = tellSummoner.instanceFor[Tell].render(t)
      val asker = askSummoner.instanceFor[Ask]
      asker(id, tellHtml, default, validation, messages(_, customContent))
    }
  }
}
