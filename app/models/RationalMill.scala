package models

import scala.collection.mutable
import scala.util._

/**
  * @author scalaprof
  */
object RationalMill {

  val conv: String => Try[Rational] = RationalMill.valueOf
  val lookup: String => Option[Rational] = RationalMill.constants.get
  implicit val store: mutable.Map[String, Rational] = mutable.Map[String, Rational]()
  implicit val parser: ExpressionParser[Rational] = new ExpressionParser[Rational](conv, lookup)

  // CONSIDER making this a class RationalMill
  def apply(): Mill[Rational] = new Mill(mutable.Stack[Rational]()) {
    def apply(s: String): Try[Rational] = RationalMill.valueOf(s)
  }

  def valueOf(s: String): Try[Rational] = Try(Rational(s))

  val constants: mutable.Map[String, Rational] = mutable.Map("e" -> Rational(BigDecimal(math.E)), "pi" -> Rational(BigDecimal(math.Pi)))
}