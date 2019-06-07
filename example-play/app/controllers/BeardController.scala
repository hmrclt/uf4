package controllers


import cats.implicits._, cats.Monoid
import javax.inject._
import ltbs.uniform._, interpreters.playframework._
import play.api.i18n.{Messages => _, _}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.nunjucks.{NunjucksRenderer, NunjucksSupport}

@Singleton
class BeardController @Inject()(
  implicit val messagesApi: MessagesApi,
  val renderer: NunjucksRenderer
) extends PlayInterpreter[Html] with I18nSupport {

  val mon: Monoid[Html] = new Monoid[Html] {
    def empty: Html = Html("")
    def combine(a: Html, b: Html) = Html(a.toString + b.toString)
  }

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

  implicit val twirlStringField = new FormField[String,Html] {
    def decode(out: Input): Either[ErrorTree,String] = {
      println(s"decode: $out")
      val root: Option[String] = out.valueAtRoot
        .flatMap(_.filter(_.trim.nonEmpty).headOption)

      root match {
        case None => Left(ErrorMsg("required").toTree)
        case Some(data) => Right(data)
      }
    }

    def encode(in: String): Input = Input.one(List(in))
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

  implicit val twirlIntField2: FormField[Int,Html] =
    twirlStringField.simap(x =>
      Either.catchOnly[NumberFormatException](x.toInt)
        .leftMap(_ => ErrorMsg("bad.value").toTree)
    )(_.toString)

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
    ): Future[common.web.PageOut[Boolean,Html]] = ???
  }

  import programs._

  def beardAction(targetId: String) = Action.async { implicit request: Request[AnyContent] =>
    val playProgram = greasy(new FuturePlayInterpreter)
    run(playProgram, targetId){ _ => Future.successful(Ok("Fin"))}
  }

}
