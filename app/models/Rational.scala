package models

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.math.Numeric.DoubleIsFractional
import scala.util.{Failure, Success, Try}

/**
 * Rational.
 *
 * This case class represents Rational numbers by a BigInt numerator and a BigInt denominator.
 * The numerator (n) and the denominator (d) may never share a common factor: if you try to construct a Rational with "new" where there is
 * a common factor, then an exception will be thrown. However, all of the apply methods ensure valid Rational instances by factoring out any such common factors.
 * Similarly, the denominator may not be negative: again, the apply methods will take care of this situation.
 *
 * The domain of Rational includes values with 0 denominator and any numerator (either -ve or +ve infinity) as well as
 * the value with 0 numerator and denominator (NaN).
 *
 * @author scalaprof
 */
case class Rational(n: BigInt, d: BigInt) {

  // Pre-conditions

  // NOTE: ensure that the denominator is positive.
  require(d >= 0L, s"Rational denominator is negative: $d")

  // NOTE: ensure that the numerator and denominator are relatively prime.
  require(n == 0L && d == 0L || Rational.gcd(n.abs, d.abs) == 1, s"Rational($n,$d): arguments have common factor: ${Rational.gcd(n, d)}")

  // Operators
  def +(that: Rational): Rational = Rational.plus(this, that)

  def +(that: BigInt): Rational = this + Rational(that)

  def +(that: Long): Rational = this + Rational(that)

  def -(that: Rational): Rational = Rational.minus(this, that)

  def -(that: BigInt): Rational = this - Rational(that)

  def unary_- : Rational = negate

  def negate: Rational = Rational.negate(this)

  def *(that: Rational): Rational = Rational.times(this, that)

  def *(that: BigInt): Rational = this * Rational(that)

  def *(that: Long): Rational = Rational.times(this, that)

  def *(that: Short): Rational = Rational.times(this, that.toLong)

  def /(that: Rational): Rational = this * that.invert

  def /(that: Long): Rational = this / Rational(that)

  def ^(that: Int): Rational = power(that)

  def root(x: Int): Rational = x match {
    case 0 => Rational.infinity
    case 1 => this
    case 2 => Rational(math.sqrt(toDouble))
    case _ => Rational(math.pow(math.E, math.log(toDouble) / x))
  }

  def ^(that: Rational): Rational = power(that.n.toInt).root(that.d.toInt)

  // Other methods appropriate to Rational
  def signum: Int = n.signum

  def invert: Rational = Rational(d, n)

  def isWhole: Boolean = d == 1L

  def isZero: Boolean = n == 0L

  def isUnity: Boolean = n == 1L && isWhole

  def isInfinity: Boolean = d == 0L

  def isNaN: Boolean = isZero && isInfinity

  def toInt: Int = Rational.toInt(this)

  def toLong: Long = Rational.toLong(this)

  def toBigInt: BigInt = Rational.toBigInt(this).get

  def toFloat: Float = Rational.toFloat(this)

  def toDouble: Double = Rational.toDouble(this)

  def power(x: Int): Rational = {
    @tailrec def inner(r: Rational, x: Int): Rational = if (x == 0) r else inner(r * this, x - 1)

    if (x == 0) Rational.one
    else {
      val rational = inner(Rational.one, math.abs(x))
      if (x > 0) rational
      else rational.invert
    }
  }

  def toBigDecimal: BigDecimal = BigDecimal(n) / BigDecimal(d)

  def compare(other: Rational): Int = Rational.compare(this, other)

  def toRationalString = s"$n/$d"

  def isExactDouble: Boolean = toBigDecimal.isExactDouble // Only work with Scala 2.11 or above

  def applySign(negative: Boolean): Rational = if (negative) negate else this

  def applyExponent(exponent: Int): Rational = this * Rational.exponent(exponent)

  override def toString: String =
    if (isNaN) "NaN"
    else if (isInfinity) (if (n > 0) "+ve" else "-ve") + " infinity"
    else if (isWhole) toBigInt.toString
    else if (d > 100000L || isExactDouble) toDouble.toString
    else toRationalString
}

object Rational {

  implicit class RationalHelper(val sc: StringContext) extends AnyVal {
    def r(args: Any*): Rational = {
      val strings = sc.parts.iterator
      val expressions = args.iterator
      val sb = new StringBuffer()
      while (strings.hasNext) {
        val s = strings.next()
        if (s.isEmpty) {
          if (expressions.hasNext)
            sb.append(expressions.next())
          else
            throw RationalException("r: logic error: missing expression")
        }
        else
          sb.append(s)
      }
      if (expressions.hasNext)
        throw RationalException(s"r: ignored: ${expressions.next()}")
      else
        Rational(sb.toString)
    }
  }

  val bigZero: BigInt = BigInt(0)
  val bigOne: BigInt = BigInt(1)
  val zero: Rational = Rational(0)
  lazy val infinity: Rational = zero.invert
  val one: Rational = Rational(1)
  val ten: Rational = Rational(10)
  val two: Rational = Rational(2)
  lazy val half: Rational = two.invert
  lazy val NaN = new Rational(0, 0)

  def apply(n: BigInt, d: BigInt): Rational = normalize(n, d)

  def apply(n: Long, d: Long): Rational = apply(BigInt(n), BigInt(d))

  def apply(n: Int, d: Int): Rational = apply(n.toLong, d.toLong)

  def apply(n: BigInt): Rational = apply(n, bigOne)

  def apply(n: BigInt, negative: Boolean): Rational = apply(n, bigOne).applySign(negative)

  def apply(n: Long): Rational = apply(n, bigOne)

  def apply(n: Int): Rational = apply(n.toLong, bigOne)

  /**
   * Method to convert a BigDecimal into a Rational.
   *
   * NOTE: that this method is also used to convert a Double into a Rational (via implicit converter from Double to BigDecimal).
   *
   * @param x the BigDecimal to convert.
   * @return a Rational which is equal to x.
   */
  def apply(x: BigDecimal): Rational =
    if (x.scale >= 0) {
      val e = BigDecimal.apply(10).pow(x.scale)
      (for (n <- (x * e).toBigIntExact; d <- e.toBigIntExact) yield Rational(n, d)) match {
        case Some(r) => r
        case None => throw RationalException(s"Rational.apply(BigDecimal): cannot represent $x as a Rational")
      }
    }
    else x.toBigIntExact match {
      case Some(b) => Rational(b)
      case None => throw RationalException(s"cannot get value from BigDecimal $x")
    }

  def apply(w: String): Rational = parse(w).get

  def parse(w: String): Try[Rational] = RationalParser.parse(w)

  /**
   * Method to process the numerator and denominator to ensure that the denominator is never zero and never shares a common factor with the numerator.
   *
   * @param n the numerator
   * @param d the denominator
   * @return a Rational formed from n and d.
   */
  @scala.annotation.tailrec
  private def normalize(n: BigInt, d: BigInt): Rational =
    if (d < 0) normalize(-n, -d) else {
      val g = gcd(n.abs, d)
      g.signum match {
        case 0 => Rational.NaN
        case _ => new Rational(n / g, d / g)
      }
    }

  @tailrec def gcd(a: BigInt, b: BigInt): BigInt = if (b == 0) a else gcd(b, a % b)

  implicit def doubleToRational(x: Double): Rational = Rational(x)

  implicit def longToRational(x: Long): Rational = Rational(x)

  implicit def bigIntToRational(x: BigInt): Rational = Rational(x)

  implicit def floatToRational(x: Float): Rational = Rational(x.toDouble)

  trait RationalIsFractional extends Fractional[Rational] {
    def plus(x: Rational, y: Rational): Rational = x + y

    def minus(x: Rational, y: Rational): Rational = x - y

    def times(x: Rational, y: Rational): Rational = x * y

    def negate(x: Rational): Rational = Rational(-x.n, x.d)

    def fromInt(x: Int): Rational = Rational(x)

    def parseString(str: String): Option[Rational] = Rational.parse(str).toOption

    def toInt(x: Rational): Int = x.toInt

    def toLong(x: Rational): Long = x.toLong

    def toFloat(x: Rational): Float = x.toFloat

    def toDouble(x: Rational): Double = x.toDouble

    //Members declared in scala.math.Fractional
    def div(x: Rational, y: Rational): Rational = Rational.div(x, y)

    // Members declared in scala.math.Ordering
    def compare(x: Rational, y: Rational): Int = x.compare(y)
  }

  private def minus(x: Rational, y: Rational): Rational = plus(x, negate(y))

  private def negate(x: Rational): Rational = Rational(-x.n, x.d)

  private def plus(x: Rational, y: Rational): Rational = Rational((x.n * y.d) + (y.n * x.d), x.d * y.d)

  private def times(x: Rational, y: Rational): Rational = Rational(x.n * y.n, x.d * y.d)

  // TODO this method can fail when x.n or x.d is too large to be represented as a Double.
  //  We need to try harder to ensure that it does not.
  private def toDouble(x: Rational): Double = {
    val top = x.n.toDouble
    val bottom = x.d.toDouble
    if (top.isInfinite) throw RationalException(s"toDouble: numerator ${x.n} cannot be represented as a Double")
    if (bottom.isInfinite) throw RationalException(s"toDouble: denominator ${x.d} cannot be represented as a Double")
    top / bottom
  }

  private def toFloat(x: Rational): Float = toDouble(x).toFloat

  private def narrow(x: Rational, max: BigInt): Try[BigInt] = for (b <- toBigInt(x); z <- narrow(b, max)) yield z

  private def narrow(x: BigInt, max: BigInt): Try[BigInt] =
    if (x.abs <= max) Success(x)
    else Failure(RationalException("narrow: loss of precision"))

  private def toLong(x: Rational): Long = (narrow(x, Long.MaxValue) map (_.toLong)).get

  private def toInt(x: Rational): Int = (narrow(x, Int.MaxValue) map (_.toInt)).get

  private def toBigInt(x: Rational): Try[BigInt] = if (x.isWhole) Success(x.n) else Failure(RationalException(s"toBigInt: $x is " + (if (x.d == 0L)
    "infinite" else "not whole")))

  private def div(x: Rational, y: Rational): Rational = x / y

  private def compare(x: Rational, y: Rational): Int = minus(x, y).signum

  def exponent(x: Int): Rational = ten.power(x)

  // CONSIDER making this private or moving back into RationalSpec
  def hasCorrectRatio(r: Rational, top: BigInt, bottom: BigInt): Boolean = {
    val _a = r * bottom
    val result = bottom == 0 || _a.isInfinity || (_a.isWhole && _a.toBigInt == top)
    if (!result) throw RationalException(s"incorrect ratio: r=${r.n}/${r.d}, y=$top, z=$bottom, _a=${_a}, gcd=${Rational.gcd(top, bottom)}")
    result
  }


  trait RationalIsRationalNumber extends RationalFractional[Rational] with RationalIsFractional {
    def asRational(t: Rational): Rational = t

    def fromRational(r: Rational): Rational = r
  }

  implicit object RationalIsRationalNumber extends RationalIsRationalNumber

  trait DoubleIsRationalNumber extends RationalFractional[Double] with DoubleIsFractional {
    def asRational(t: Double): Rational = Rational.doubleToRational(t)

    def fromRational(r: Rational): Double = r.toDouble

    def compare(x: Double, y: Double): Int = x.compare(y)
  }

  implicit object DoubleIsRationalNumber extends DoubleIsRationalNumber
}

case class RationalException(s: String) extends Exception(s)

trait RationalFractional[T] extends Fractional[T] {
  def asRational(t: T): Rational

  def fromRational(r: Rational): T
}
