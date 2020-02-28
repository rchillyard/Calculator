package application

import play.api._
import play.api.mvc._
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

import akka.actor.ActorRef
import com.typesafe.config.{ ConfigFactory, Config }
import actors._
import models._

object Application {
  def getSetupForDouble(implicit system: ActorSystem) = {
		  implicit val lookup: String=>Option[Double] = DoubleMill.constants.get
      implicit val conv: String=>Try[Double] = DoubleMill.valueOf
			implicit val parser = new ExpressionParser[Double](conv,lookup)
			val mill: Mill[Double] = DoubleMill()
			// Note: the following pattern should NOT be used within an actor
      val props = Props(new Calculator(mill,parser))
			(props,"doubleCalculator","Double Calculator")
  }
  // CONSIDER This assumes that we have controllers.Rational in our classpath already.
  // I'd like to try the possibility of dynamically loading the controllers.Rational stuff.
  // But, that's going to be very tricky, so we'll leave it for now.
    def getSetupForRational(implicit system: ActorSystem) = {
      implicit val lookup: String=>Option[Rational] = RationalMill.constants.get
      implicit val conv: String=>Try[Rational] = RationalMill.valueOf
      implicit val parser = new ExpressionParser[Rational](conv,lookup)
      val mill: Mill[Rational] = RationalMill()
      // Note: the following pattern should NOT be used within an actor
      val props = Props(new Calculator(mill,parser))
      (props,"rationalCalculator","controllers.Rational Calculator")
  }
}
