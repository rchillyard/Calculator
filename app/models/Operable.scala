package models

import models.Rational.RationalIsFractional

import scala.math.Numeric.DoubleIsFractional

trait Operable[T] extends Fractional[T] {

  def power(x: T, exp: Int): T

  def log(x: T): T
}

object Operable {

  implicit object DoubleIsOperable extends DoubleIsFractional with Operable[Double] {
    def power(x: Double, exp: Int): Double = math.pow(x, exp)

    def log(x: Double): Double = math.log(x)

    def compare(x: Double, y: Double): Int = x.compareTo(y)
  }


  implicit object RationalIsOperable extends RationalIsFractional with Operable[Rational] {
    def power(x: Rational, exp: Int): Rational = x.power(exp)

    def log(x: Rational): Rational = Rational(math.log(x.toDouble))
  }

}