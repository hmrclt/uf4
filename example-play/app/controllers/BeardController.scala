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
    breadcrumbs: List[String],
    request: Request[AnyContent],
    messages: UniformMessages[Html]
  ): Html = 
      views.html.chrome(key.mkString("."), errors, Html(tell.toString + ask.toString), breadcrumbs.drop(1))(messages, request)

  def renderForm(
    key: List[String],
    errors: ErrorTree,
    form: Html,
    breadcrumbs: List[String],
    request: Request[AnyContent],
    messagesIn: UniformMessages[Html]
  ): Html = {
    views.html.chrome(key.last, errors, form, breadcrumbs)(messagesIn, request)
  }

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
      path: List[String],
      data: Option[Input],
      errors: ErrorTree,
      messages: UniformMessages[Html]
    ): Html = {
      println(s"data: $data")
      val existingValue: String = data.flatMap(_.valueAtRoot.flatMap{_.headOption}).getOrElse("")
      views.html.uniform.string(path.mkString("."), existingValue, errors, messages)
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
      path: List[String],
      db: DB,
      messages: UniformMessages[Html]
    ): Future[PageOut[Boolean,Html]] = ???
  }

  import programs._

  def beardAction(path: String) = Action.async { implicit request: Request[AnyContent] =>
    val playProgram = greasy(new FuturePlayInterpreter)
    run(playProgram, path){ _ => Future.successful(Ok("Fin"))}
  }

  def stupid(path: String) = Action.async { implicit request: Request[AnyContent] =>

    implicit def autoField[A] = new FormField[A,Html] {
      def decode(out: ltbs.uniform.Input): Either[ltbs.uniform.ErrorTree,A] = ???
      def encode(in: A): ltbs.uniform.Input = ???
      def render(path: List[String],data: Option[ltbs.uniform.Input],errors: ltbs.uniform.ErrorTree,messages: ltbs.uniform.UniformMessages[play.twirl.api.Html]): play.twirl.api.Html = ???
    }

    val playProgram = greasy2(new FuturePlayInterpreter)
    run(playProgram, path){ _ => Future.successful(Ok("Fin"))}
  }


}
