package ltbs.uniform
package interpreters.playframework

trait GenericWebTell[A,Html] {
  def render(in: A): Html
}
