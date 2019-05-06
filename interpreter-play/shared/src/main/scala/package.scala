package ltbs.uniform.interpreters

import cats.data._
import play.api.mvc.{ Request, Result, AnyContent }
import scala.concurrent.Future

package object playframework {

  // New
  type Path = List[String]
  type JourneyConfig = String
  type WebInner[A] = RWST[Future, (JourneyConfig, String, Request[AnyContent]), Unit, (Path, DB), A]
  type WebMonad[A] = EitherT[WebInner, Result, A]

  type ErrorTree = Map[String,String]
  type DB = Map[String,String]
}
