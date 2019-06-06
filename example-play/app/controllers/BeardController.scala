package controllers


import cats.implicits._
import cats.kernel.Monoid
import javax.inject._
import ltbs.uniform._, interpreters.playframework._
import play.api._
import play.api.i18n.{Messages => _, _}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import play.twirl.api.{Html, HtmlFormat}

@Singleton
class BeardController @Inject()(implicit val messagesApi: MessagesApi) extends PlayInterpreter[Html] with I18nSupport {

  def messages(request: Request[AnyContent], customContent: Map[String,(String, List[Any])]): UniformMessages[Html] = (
//    convertMessages(messagesApi.preferred(request)) |+|
      UniformMessages.bestGuess.map(HtmlFormat.escape)
  )

  def pageChrome(
    key: List[String],
    errors: ErrorTree,
    tell: Html,
    ask: Html,
    breadcrumbs: Path,
    request: Request[AnyContent],
    messages: UniformMessages[Html]
  ): Html = 
      views.html.chrome(key.mkString("."), errors, Html(tell.toString + ask.toString), breadcrumbs)(messages, request)

  implicit val persistence: PersistenceEngine = DebugPersistence(UnsafePersistence())

  implicit val twirlIntField = new FormField[Int,Html] {
    def decode(out: Input): Either[ErrorTree,Int] = {
      println(s"decode: $out")
      val root: Option[String] = out.valueAtRoot
        .flatMap(_.filter(_.trim.nonEmpty).headOption)

      root match {
        case None => Left(ErrorMsg("required").toTree)
        case Some(data) =>
          Either.catchOnly[NumberFormatException](data.toInt)
            .leftMap(_ => ErrorMsg("bad.value").toTree)
      }
    }

    def encode(in: Int): Input = Input.one(List(in.toString))
    def render(
      key: List[String],
      path: Path,
      data: Option[Input],
      errors: ErrorTree,
      messages: UniformMessages[Html]
    ): Html = {
      println(s"data: $data")
      val existingValue: String = data.flatMap(_.valueAtRoot.flatMap{_.headOption}).getOrElse("")
      views.html.uniform.string(key, existingValue, errors, messages)
    }
  }

  implicit val askBoolean = new PlayAsk[Boolean] {
    def page(
      targetId: List[String],
      currentId: List[String],
      default: Option[Boolean],
      validation: List[List[Rule[Boolean]]],
      config: JourneyConfig,
      submittedData: Option[Input],
      path: Path,
      db: DB,
      messages: UniformMessages[Html]
    ): Future[PageOut[Boolean,Html]] = ???
  }

  import programs._

  def beardAction(targetId: String) = Action.async { implicit request: Request[AnyContent] =>
    val playProgram = greasy(new FuturePlayInterpreter)
    run(playProgram, targetId){ _ => Future.successful(Ok("Fin"))}
  }

}
