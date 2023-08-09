package models

import com.phasmidsoftware.number.mill.{Mill, MillException}
import com.phasmidsoftware.number.parse.NumberParser
import com.phasmidsoftware.number.core.Number
import com.phasmidsoftware.number.core.Number.NumberIsNumeric
import com.phasmidsoftware.number.core.Rational.RationalFractional

import scala.collection.mutable
import scala.util.Try

class NumberMill(implicit store: mutable.Map[String, Number], ev: RationalFractional[Number]) extends models.Mill[Number](mutable.Stack[Number]()) {
  def apply(s: String): Try[Number] = NumberMill.valueOf(s)
}

/**
 * @author scalaprof
 */
object NumberMill {
  // NOTE: this depends on com.phasmidsoftware.number.core.Number
  val constants: mutable.Map[String, Number] = mutable.Map(
    "e" -> com.phasmidsoftware.number.core.Constants.e,
    "pi" -> com.phasmidsoftware.number.core.Constants.pi
  )
  implicit val parser: NumberParser = new NumberParser
    val conv: String=>Try[Number] = parser.parseNumber
    val lookup: String=>Option[Number] = constants.get
    implicit val store = mutable.Map[String,Number]()
//      new ExpressionParser[com.phasmidsoftware.number.core.Number](conv,lookup)
  implicit object RationalFractionalNumber extends RationalFractional[Number] with NumberIsNumeric {
  def asRational(t: Number): Rational = t.toRational match {
    case Some(r) => Rational(r.n, r.d)
    case None => throw new MillException(s"$t cannot be represented as a rational number")
  }

  def fromRational(r: Rational): Number = Number(com.phasmidsoftware.number.core.Rational(r.n, r.d))

  def div(x: Number, y: Number): Number = x / y
}

def apply(): models.Mill[Number] = new models.Mill(mutable.Stack[Number]()) {
  def apply(s: String): Try[Number] = NumberMill.conv(s)


}
  def valueOf(s: String): Try[Number] = conv(s)

    //      def apply(s: String): Try[com.phasmidsoftware.number.core.Number] = NumberMill.valueOf(s)
//    }
//    def valueOf(s: String): Try[com.phasmidsoftware.number.core.Number] = Try(Number(s))

}