package actors

import akka.actor.{Actor, ActorLogging}
import models._

/**
 * @author scalaprof
 *
 * CONSIDER making parser implicit
 */
class Calculator[A : Numeric](mill: Mill[A], parser: ExpressionParser[A]) extends Actor with ActorLogging {
  
  override def receive: Receive = {
    case View =>
      log.info(s"Calculator replying with mill contents: $mill")
      sender() ! mill
    case x: String =>
      log.info(s"Calculator received $x")
      try {
        val response = mill.parse(x)(parser)
        log.info(s"response: $response")
        sender() ! response
      }
      catch {
        case t: Throwable => println("should never hit this line"); log.error(t, "logic error: should never log this issue")
      }
    case z =>
      log.warning(s"received unknown message type: $z")
  }
}

object View