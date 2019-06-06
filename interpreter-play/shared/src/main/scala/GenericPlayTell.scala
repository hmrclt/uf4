package ltbs.uniform
package interpreters.playframework

trait GenericPlayTell[A,Html] {
  def render(in: A): Html
}
