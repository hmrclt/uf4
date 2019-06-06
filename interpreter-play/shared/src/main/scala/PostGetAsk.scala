package ltbs.uniform
package interpreters.playframework

import concurrent.Future

trait FormField[A, Html]{
  def encode(in: A): Input
  def decode(out: Input): Either[ErrorTree,A]
  def render(
    path: List[String],
    data: Option[Input],
    errors: ErrorTree,
    messages: UniformMessages[Html]
  ): Html
}

class SimpleForm[A, Html](field: FormField[A, Html]) extends GenericPlayAsk[A, Html] {

  def page(
    targetId: List[String], // the uri the user agent is asking for
    currentId: List[String], // the current step in the journey
    default: Option[A],
    validation: List[List[Rule[A]]],
    config: JourneyConfig,
    submittedData: Option[Input],
    path: List[String],
    db: DB,
    messages: UniformMessages[Html]
  ): Future[PageOut[A,Html]] = {
    import cats.implicits._
    lazy val rawDbData: Option[String] = db.get(targetId)
    lazy val dbObject: Option[Either[ErrorTree,A]] =
      rawDbData map {Input.fromUrlEncodedString(_) >>= field.decode >>= validation.combined.either}

    println(s"PATH: $targetId === $currentId")

    println("KEY:" + currentId.lastOption.toString)
    val localData = currentId.lastOption match {
      case Some(key) => submittedData.map(_.subTree(key))
      case None => submittedData
    }
    println("localData:" + localData)

    if (targetId === currentId) {
      localData match {
        case None =>
          val errors: ErrorTree = dbObject match {
            case Some(Left(e)) => e
            case _ => ErrorTree.empty
          }

          val prepopulatedData: Option[Input] =
            rawDbData
              .map(Input.fromUrlEncodedString)
              .flatMap(_.toOption)
              .orElse(default.map{x => field.encode(x)})

          val result = field.render(path, prepopulatedData, errors, messages)
          Future.successful(PageOut(currentId, db, AskResult.Payload(result, errors)))
        case Some(rawPostData) =>
          val postObject: Either[ErrorTree,A] =
            field.decode(rawPostData) >>= validation.combined.either

          postObject match {
            case Left(errors) =>
              val result = field.render(path, Some(rawPostData), errors, messages)
              Future.successful(PageOut(currentId, db, AskResult.Payload(result, errors)))
            case Right(o) =>
              Future.successful(PageOut(currentId, db + (currentId -> rawPostData.toUrlEncodedString), AskResult.Success(o)))
          }
      }
    } else {
      dbObject match {
        case Some(Right(data)) =>
          // they're replaying the journey
          Future.successful(PageOut(currentId, db, AskResult.Success(data)))
        case _ =>
          // either trying to jump ahead (None) or data no longer validates (code change or corruption)
          Future.successful(PageOut(currentId, db, AskResult.GotoPath(targetId)))
      }
    }
  }

}
