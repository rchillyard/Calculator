package controllers

import actors.View
import javax.inject._
import akka.actor.ActorSystem
import akka.util.Timeout
import application.Application
import com.typesafe.config.ConfigFactory
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
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

/**
 * This controller creates an `Action` that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 *
 * @param cc standard controller components
 * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
 * run code after a delay.
 * @param exec We need an `ExecutionContext` to execute our
 * asynchronous code.  When rendering content, you should use Play's
 * default execution context, which is dependency injected.  If you are
 * using blocking operations, such as database or network access, then you should
 * use a different custom execution context that has a thread pool configured for
 * a blocking API.
 */
@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

//  /**
//   * Creates an Action that returns a plain text message after a delay
//   * of 1 second.
//   *
//   * The configuration in the `routes` file means that this method
//   * will be called when the application receives a `GET` request with
//   * a path of `/message`.
//   */
//  def message = Action.async {
//    getFutureMessage(1.second).map { msg => Ok(msg) }
//  }
//
//  private def getFutureMessage(delayTime: FiniteDuration): Future[String] = {
//    val promise: Promise[String] = Promise[String]()
//    actorSystem.scheduler.scheduleOnce(delayTime) {
//      promise.success("Hi!")
//    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
//    promise.future
//  }

  val config = ConfigFactory.load()
  val which = config.getString("calculator")

  implicit val timeout: Timeout = Timeout(10 seconds)
  implicit val system = ActorSystem("RPN-Calculator")
  val setup = which match {
    case "rational" => Application.getSetupForRational
    case "double" => Application.getSetupForDouble
    case _ => Console.err.println(s"Unsupported calculator type: $which"); Application.getSetupForRational
  }
  val calculator = system.actorOf(setup _1,setup _2)
  val name: String = setup _3;
  println(s"$name is ready")

  def message = Action.async {
    val xsf = (calculator ? View).mapTo[Seq[_]]
    xsf map {
      case xs => Ok(s"$name: calculator has the nothing at all: $xs")
    }
  }

  def command(s: String) = Action.async {
    val xtf = (calculator ? s).mapTo[Try[_]]
    xtf map {
      case Success(x) => Ok(s"""$name: you entered "$s" and got back $x""")
      case Failure(e) => if (s=="clr") Ok("$name: cleared") else Ok(s"""$name: you entered "$s" which caused error: $e""")
      //      case Failure(e) => if (s=="clr") redirect("/") else  Ok(s"""$name: you entered "$s" which caused error: $e""")
    }
  }


}
