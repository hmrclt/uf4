package govukscalatags.generic

import scalatags._, generic._
import ltbs.uniform._, common.web._
import cats.implicits._

abstract class ScalatagsGovukWidgets[Builder, Output <: FragT, FragT](
  val bundle: Bundle[Builder, Output, FragT]
) {
  import bundle.all._

  implicit val stringField = new FormField[String,Tag] {

    def decode(out: Input): Either[ErrorTree,String] = {
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
      messages: UniformMessages[Tag]
    ): Tag = {

      val existingValue: String =
        data.flatMap(_.valueAtRoot.flatMap{_.headOption}).getOrElse("")

      val fieldId=key.mkString("-")

      div(cls:="govuk-form-group") {
        label(cls:="govuk-label", attr("for"):=fieldId) {
          messages(fieldId)
        }
        input(
          cls:="govuk-input",
          id:=fieldId,
          name:=key.mkString("."),
          `type`:="text",
          value:=existingValue
        )
      }
      
    }
  }

  implicit val intField: FormField[Int,Tag] =
    stringField.simap(x =>
      Either.catchOnly[NumberFormatException](x.toInt)
        .leftMap(_ => ErrorMsg("bad.value").toTree)
    )(_.toString)

  implicit val longField: FormField[Long,Tag] =
    stringField.simap(x =>
      Either.catchOnly[NumberFormatException](x.toLong)
        .leftMap(_ => ErrorMsg("bad.value").toTree)
    )(_.toString)

  implicit val floatField: FormField[Float,Tag] =
    stringField.simap(x =>
      Either.catchOnly[NumberFormatException](x.toFloat)
        .leftMap(_ => ErrorMsg("bad.value").toTree)
    )(_.toString)

  implicit val doubleField: FormField[Double,Tag] =
    stringField.simap(x =>
      Either.catchOnly[NumberFormatException](x.toDouble)
        .leftMap(_ => ErrorMsg("bad.value").toTree)
    )(_.toString)
  
}
