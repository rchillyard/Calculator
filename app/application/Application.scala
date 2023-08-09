package application

import actors._
import akka.actor.{ActorSystem, Props}
import models.NumberMill.RationalFractionalNumber

import scala.collection.mutable
//import com.phasmidsoftware.number.model.Number
import models._

import scala.util._

// CONSIDER making this part of the services package
object Application {
  def getSetupForDouble(implicit system: ActorSystem): (Props, String, String) = {
		  implicit val lookup: String=>Option[Double] = DoubleMill.constants.get
    implicit val conv: String => Try[Double] = DoubleMill.valueOf
    implicit val parser: ExpressionParser[Double] = new ExpressionParser[Double](conv, lookup)
    val mill: Mill[Double] = DoubleMill()
    // Note: the following pattern should NOT be used within an actor
    val props = Props(new Calculator(mill, parser))
    (props, "doubleCalculator", "Double Calculator")
  }

  // CONSIDER This assumes that we have controllers.Rational in our classpath already.
  // I'd like to try the possibility of dynamically loading the controllers.Rational stuff.
  // But, that's going to be very tricky, so we'll leave it for now.
  def getSetupForRational(implicit system: ActorSystem): (Props, String, String) = {
    implicit val lookup: String => Option[Rational] = RationalMill.constants.get
    implicit val conv: String => Try[Rational] = RationalMill.valueOf
    implicit val parser: ExpressionParser[Rational] = new ExpressionParser[Rational](conv, lookup)
    val mill: Mill[Rational] = RationalMill()
    // Note: the following pattern should NOT be used within an actor
    val props = Props(new Calculator(mill, parser))
    (props, "rationalCalculator", "controllers.Rational Calculator")
  }

  // CONSIDER This assumes that we have controllers.Number in our classpath already.
    def getSetupForNumber(implicit system: ActorSystem) = {
      implicit val lookup: String => Option[com.phasmidsoftware.number.core.Number] = NumberMill.lookup
      implicit val conv: String => Try[com.phasmidsoftware.number.core.Number] = NumberMill.conv
      implicit val parser = NumberMill.parser
      implicit val store: mutable.Map[String, com.phasmidsoftware.number.core.Number] = mutable.Map[String, com.phasmidsoftware.number.core.Number]()
      import NumberMill.RationalFractionalNumber
      val mill: Mill[com.phasmidsoftware.number.core.Number] = new Mill(mutable.Stack[com.phasmidsoftware.number.core.Number]()) {
        def apply(s: String): Try[Rational] = RationalMill.valueOf(s)
      }
      // Note: the following pattern should NOT be used within an actor
      val props = Props(new Calculator(mill, parser))
      (props,"NumberCalculator","controllers.Number Calculator")
    }
}
