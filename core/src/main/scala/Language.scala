package ltbs.uniform

import shapeless.{HNil => _, `::` => _, _}, ops.hlist.Selector
import reflect.runtime.universe.WeakTypeTag
import scala.language.higherKinds

// type ErrorTree2 = List[(Error, NEL[Path])]

trait Language[UF[_], SupportedTell <: HList, SupportedAsk <: HList]{

  def interact[Tell: WeakTypeTag, Ask: WeakTypeTag](
    id: String,
    tell: Tell,
    default: Option[Ask] = None,
    validation: List[List[Rule[Ask]]] = Nil,
    customContent: Map[String,(String,List[Any])] = Map.empty
  )(
    implicit
    selectorTell : Selector[SupportedTell, Tell],
    selectorAsk : Selector[SupportedAsk, Ask]
  ): UF[Ask]

  def ask[A: WeakTypeTag](
    id: String,
    default: Option[A] = None,    
    validation: List[List[Rule[A]]] = Nil,
    customContent: Map[String,(String,List[Any])] = Map.empty    
  )(
    implicit selectorAsk : Selector[SupportedAsk, A],
    selectorTell : Selector[SupportedTell, Unit]
  ): UF[A] = interact[Unit,A](id, (), default, validation, customContent)

  def tell[A: WeakTypeTag](
    id: String,
    t: A,
    customContent: Map[String,(String,List[Any])] = Map.empty    
  )(
    implicit selectorAsk : Selector[SupportedAsk, Unit],
    selectorTell : Selector[SupportedTell, A]
  ): UF[Unit] = interact[A,Unit](id, t, customContent=customContent)

}
