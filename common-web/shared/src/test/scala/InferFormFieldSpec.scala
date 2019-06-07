package ltbs.uniform
package common.web

import org.scalatest._
import cats.implicits._
import cats.Monoid

object StringForms extends InferFormField[String]  {
  val mon = implicitly[Monoid[String]]


  implicit val stringIntField = new FormField[Int,String] {
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
      messages: UniformMessages[String]
    ): String = {
      val existingValue: String = data.flatMap(_.valueAtRoot.flatMap{_.headOption}).getOrElse("")
      s"${key}: ${existingValue} \n"
    }
  }    

}

class InferFormFieldSpec extends FlatSpec with Matchers {
  import StringForms._
  "A direct form field" should "have the property decode(encode(x)) = Right(x)" in {

    {
      val a = implicitly[FormField[Int,String]]
      val d = a.decode(a.encode(5))
      d should be ((5).asRight[ErrorTree])
    }
  }

  "An inferred form field" should "have the property decode(encode(x)) = Right(x)" in {

    {
      val testFormField = implicitly[FormField[(Int,Int),String]]
      import testFormField._
      decode(encode((1,1))) should be (((1,1)).asRight[ErrorTree])
    }
  }

}
