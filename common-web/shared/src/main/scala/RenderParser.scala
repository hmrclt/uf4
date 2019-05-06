package ltbs.uniform
package common

trait RenderReaderEncoder[IN,ASK,ENCODED,ERROR,OUT] {

  def encode(in: ASK): ENCODED
  def decode(in: ENCODED): Either[ERROR,ASK]

  def read(in: IN): Either[ERROR,ASK]

  def render(
    key: String,
    data: Option[ASK],
    tell: OUT, 
    messages: UniformMessages[OUT]
  ): OUT
}

trait RenderReader[IN,ASK,ERROR,OUT] extends RenderReaderEncoder[IN,ASK,ASK,ERROR,OUT] {
  def encode(in: ASK): ASK = in
  def decode(in: ASK): Either[ERROR,ASK] = Right(in)
}
