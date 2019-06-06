package ltbs.uniform
package interpreters.playframework

import concurrent.Future

sealed trait AskResult[A,Html]
object AskResult {
  case class GotoPath[A,Html](path: List[String]) extends AskResult[A,Html]
  case class Payload[A,Html](html: Html, errors: ErrorTree) extends AskResult[A,Html]
  case class Success[A,Html](objectOut: A) extends AskResult[A,Html]    
}

case class PageOut[A,Html](
  path: Path,
  db: DB,
  output: AskResult[A,Html]
)

trait GenericPlayAsk[A,Html] {

  def page(
    targetId: List[String],
    currentId: List[String],
    default: Option[A],
    validation: List[List[Rule[A]]],
    config: JourneyConfig,
    submittedData: Option[Input],
    path: Path,
    db: DB,
    messages: UniformMessages[Html]
  ): Future[PageOut[A,Html]]

}
