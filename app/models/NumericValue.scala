package models

import java.util.NoSuchElementException

import scala.util._

case class NumericValue(value: Either[Either[Either[Option[Double], Rational], BigInt], Int]) {

  def isValid: Boolean = toDouble.isDefined

  def toDouble: Option[Double] = NumericValue.optionMap(value)(_.toDouble, x => NumericValue.optionMap(x)(_.toDouble, y => NumericValue.optionMap(y)(_.toDouble, identity)))

  def toRational: Option[Rational] = {
    val result: Try[Rational] = NumericValue.tryMap(value)(Rational(_), x => NumericValue.tryMap(x)(Rational(_), y => NumericValue.tryMap(y)(x => x, {
      case Some(n) => Success(n)
      case None => Failure(new NoSuchElementException())
    })))
    result.toOption
  }

  override def toString: String = {
    NumericValue.optionMap(value)(_.toString, x => NumericValue.optionMap(x)(_.toString, y => NumericValue.optionMap(y)(_.toString, {
      case Some(n) => Some(n.toString)
      case None => None
    }))).getOrElse("<undefined>")
  }
}

object NumericValue {
  def apply(x: Int): NumericValue = NumericValue(Right(x))

  def apply(x: BigInt): NumericValue = NumericValue(Left(Right(x)))

  def apply(x: Rational): NumericValue = NumericValue(Left(Left(Right(x))))

  def apply(x: Double): NumericValue = NumericValue(Left(Left(Left(Some(x)))))

  def apply(): NumericValue = NumericValue(Left(Left(Left(None))))

  def parse(w: String): Try[NumericValue] = {
    val value: NumericValue = new NumericValue(optionToEither(w.toIntOption, optionToEither(Try(BigInt(w)).toOption, optionToEither(Rational.parse(w).toOption, w.toDoubleOption))))
    if (value.isValid) Success(value) else Failure(NumericException(s"cannot parse $w as a NumericValue"))
  }

  implicit object NumericValueIsFractional extends Fractional[NumericValue] {
    def div(x: NumericValue, y: NumericValue): NumericValue = (for (rx <- x.toRational; ry <- y.toRational) yield NumericValue(rx + ry)).getOrElse((for (rx <- x.toDouble; ry <- y.toDouble) yield NumericValue(rx + ry)).getOrElse(NumericValue()))

    def plus(x: NumericValue, y: NumericValue): NumericValue = ???

    def minus(x: NumericValue, y: NumericValue): NumericValue = ???

    def times(x: NumericValue, y: NumericValue): NumericValue = ???

    def negate(x: NumericValue): NumericValue = ???

    def fromInt(x: Int): NumericValue = ???

    def parseString(str: String): Option[NumericValue] = NumericValue.parse(str).toOption

    def toInt(x: NumericValue): Int = ???

    def toLong(x: NumericValue): Long = ???

    def toFloat(x: NumericValue): Float = ???

    def toDouble(x: NumericValue): Double = ???

    def compare(x: NumericValue, y: NumericValue): Int = ???
  }

  private def optionToEither[X, Y](x: Option[X], y: => Y): Either[Y, X] = x.map(Right(_)).getOrElse(Left(y))

  private def tryMap[X, Y, Z](xYe: Either[X, Y])(yToZ: Y => Z, xToZy: X => Try[Z]): Try[Z] = xYe.toOption.map(yToZ) match {
    case Some(z) => Success(z)
    case None => xYe.left.toOption.map(xToZy) match {
      case Some(z) => z
      case None => Failure(new NoSuchElementException)
    }
  }

  private def optionMap[X, Y, Z](xYe: Either[X, Y])(yToZ: Y => Z, xToZy: X => Option[Z]): Option[Z] = xYe.toOption.map(yToZ) match {
    case Some(z) => Some(z)
    case None => xYe.left.toOption.flatMap(xToZy)
  }
}

case class NumericException(str: String) extends Exception(str)

//case class NumericValue(w: String) {
//  lazy val maybeInt: Option[Int] = w.toIntOption
//  lazy val maybeRational: Option[Rational] = Rational.parse(w).toOption
//  lazy val maybeBigInt: Option[BigInt] = Try(BigInt(w)).toOption
//  lazy val maybeBigDecimal: Option[BigDecimal] = Try(BigDecimal(w)).toOption
//  lazy val maybeDouble: Option[Double] = w.toDoubleOption
//
//  def isIntegral: Boolean = maybeInt.isDefined
//  def isRational: Boolean = maybeRational.isDefined
//  def isBigInt: Boolean = maybeBigInt.isDefined
//  def isBigDecimal: Boolean = maybeBigDecimal.isDefined
//
//  import Rational._
//  def toRational: Rational = maybeInt.map(Rational(_)).getOrElse(maybeBigInt.map(Rational(_)).getOrElse(maybeBigDecimal.map(Rational(_))))
//}

