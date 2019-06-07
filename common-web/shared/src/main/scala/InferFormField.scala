package ltbs.uniform
package common.web

import shapeless._, labelled._
import cats.implicits._
import cats.Monoid

trait InferFormField[Html] {

  val mon: Monoid[Html]

  type FF[A] = FormField[A, Html]


  implicit val hnilField = new FF[HNil] {
    def decode(out: Input): Either[ErrorTree,HNil] = Right(HNil)
    def encode(in: HNil): Input = Input.empty
    def render(
      key: List[String],
      path: Path,
      data: Option[Input],
      errors: ErrorTree,
      messages: UniformMessages[Html]
    ): Html = mon.empty
  }

  implicit def hConsField[K <: Symbol, H, T <: HList](
    implicit
      witness: Witness.Aux[K],
    hField: Lazy[FF[H]],
    tField: FF[T]
  ): FF[FieldType[K,H] :: T] = new FF[FieldType[K,H] :: T] {
    val fieldName: String = witness.value.name
    def decode(out: Input): Either[ErrorTree,FieldType[K,H] :: T] = {
      (
        hField.value.decode(out / fieldName),
        tField.decode(out)
      ) match {
        case (Right(h), Right(t)) => Right((field[K](h) :: t))
        case (Left(he), Left(te)) => Left(he.prefixWith(fieldName) |+| te)
        case (_,        Left(te)) => Left(te)
        case (Left(he), _)        => Left(he.prefixWith(fieldName))
      }
    }

    def encode(a: FieldType[K,H] :: T): Input = {
      val tailData: Input = tField.encode(a.tail)
      val headData: Input = hField.value.encode(a.head)
      tailData |+| headData.prefixWith(fieldName)
    }

    def render(
      key: List[String],
      path: Path,
      data: Option[Input],
      errors: ErrorTree,
      messages: UniformMessages[Html]
    ): Html = mon.combine(
      hField.value.render(key :+ fieldName, path, data.map{_ / fieldName}, errors / fieldName, messages),
      tField.render(key, path, data, errors, messages)
    )
  }
  
  implicit def genericField[A, H, T](implicit
    generic: LabelledGeneric.Aux[A,T],
    hGenParser: Lazy[FF[T]],
    lp: LowPriority
  ): FF[A] =
    hGenParser.value.imap(generic.from)(generic.to)

  // COPRODUCTS

  /*
  def selectionOfFields(
    inner: List[(String, (String, Input, ErrorTree, UniformMessages[Html]) => Html)]
  )(
    key: String,
    values: Input,
    errors: ErrorTree,
    messages: UniformMessages[Html]
  ): Html
   */

  /*
  case class CoproductFieldList[A](
    inner: List[(String, (String, Input, ErrorTree, UniformMessages[Html]) => Html)]
  )
  implicit val cnilField: CoproductFieldList[CNil] = CoproductFieldList(List.empty)

  implicit def coproductFieldList[K <: Symbol, H, T <: Coproduct](
    implicit
      witness: Witness.Aux[K],
    hField: FF[H],
    tFields: CoproductFieldList[T]
  ): CoproductFieldList[FieldType[K, H] :+: T] = CoproductFieldList (
    (witness.value.name, hField.render _) :: tFields.inner
  )

  implicit def coproductField[A](implicit coproductFields: CoproductFieldList[A]) =
    new FF[A] {
      def render(key: String, values: Input, errors: ErrorTree, messages: UniformMessages[Html]): Html =
        selectionOfFields(coproductFields.inner)(key,values,errors,messages)
    }
   */
}
