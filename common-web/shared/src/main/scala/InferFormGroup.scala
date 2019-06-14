package ltbs.uniform
package common.web

import shapeless._, labelled._
import cats.implicits._
import cats.Monoid


trait InferFormGroup[Html] {


  type FF[A] = FormField[A, Html]
}
