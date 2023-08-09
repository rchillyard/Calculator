package models

import scala.util.Try

case class NumericToken(s: String) {

  def toNumeric[N: Numeric]: Option[N] = implicitly[Numeric[N]].parseString(s)

  def toFractional[N: Fractional]: Option[N] = implicitly[Fractional[N]].parseString(s)

}
