package models

import scala.collection.mutable
import scala.util._
//

/**
 * @author scalaprof
 */
object DoubleMill {
  val conv: String => Try[Double] = DoubleMill.valueOf
  val lookup: String => Option[Double] = DoubleMill.constants.get
  implicit val store: mutable.Map[String, Double] = mutable.Map[String, Double]()
  implicit val parser: ExpressionParser[Double] = new ExpressionParser[Double](conv, lookup)

  import models.Rational.DoubleIsRationalNumber

  def apply(): Mill[Double] = new Mill(mutable.Stack[Double]()) {
    def apply(s: String): Try[Double] = DoubleMill.valueOf(s)
  }

  def valueOf(s: String): Try[Double] = Try(s.toDouble)

  val constants: mutable.Map[String, Double] = mutable.Map("e" -> math.E, "pi" -> math.Pi)
}