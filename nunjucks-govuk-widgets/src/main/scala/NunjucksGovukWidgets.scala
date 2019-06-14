package doofer

import cats.implicits._, cats.Monoid
import ltbs.uniform._, common.web._
import play.api.libs.json._
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.NunjucksRenderer
import scala.util._
import scala.language.implicitConversions

class NunjucksGovukWidgets(
  val renderer: NunjucksRenderer  
) extends InferFormField[Html] {

  implicit def convertTry[A](in: Try[A]): A = in match {
    case Failure(e) => throw e
    case Success(s) => s
  }

  val mon: Monoid[Html] = new Monoid[Html] {
    def empty: Html = Html("")
    def combine(a: Html, b: Html) = Html(a.toString + b.toString)
  }  

  implicit val stringField = new FormField[String,Html] {
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

      implicit val dummyRequest = new play.api.mvc.RequestHeader {
        def attrs: play.api.libs.typedmap.TypedMap = ???
        def connection: play.api.mvc.request.RemoteConnection = ???
        def headers: play.api.mvc.Headers = ???
        def method: String = ???
        def target: play.api.mvc.request.RequestTarget = ???
        def version: String = ???
      }

      val existingValue: String = data.flatMap(_.valueAtRoot.flatMap{_.headOption}).getOrElse("")

      val context = Json.obj(
        "foo" -> "bar"
      )

      renderer.render("myView.njk", context)

    }
  }



}
