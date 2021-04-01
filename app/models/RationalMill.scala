package models

import scala.collection.mutable.{Map, Stack}
import scala.util._

/**
  * @author scalaprof
  */
object RationalMill {

  val conv: String=>Try[Rational] = RationalMill.valueOf
  val lookup: String=>Option[Rational] = RationalMill.constants.get
  implicit val store = Map[String,Rational]()
  implicit val parser = new ExpressionParser[Rational](conv,lookup)
  // CONSIDER making this a class RationalMill
  def apply(): Mill[Rational] = new Mill(Stack[Rational]()) {
    def apply(s: String): Try[Rational] = RationalMill.valueOf(s)
  }
  def valueOf(s: String): Try[Rational] = Try(Rational(s))
  val constants = Map("e"->Rational(BigDecimal(math.E)), "pi"->Rational(BigDecimal(math.Pi)))
}