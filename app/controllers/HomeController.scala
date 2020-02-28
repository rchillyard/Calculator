package controllers

import javax.inject._
import play.api.mvc._
import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._
import akka.actor.ActorRef
import com.typesafe.config.{Config, ConfigFactory}
import actors._
import application.Application
import models._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

//  /**
//   * Create an Action to render an HTML page with a welcome message.
//   * The configuration in the `routes` file means that this method
//   * will be called when the application receives a `GET` request with
//   * a path of `/`.
//   */
//  def index = Action {
//    Ok(views.html.index("Your CSYE7200 Play application is ready."))
//  }

  val config = ConfigFactory.load()
  val which = config.getString("calculator")

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
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

  def index() = Action.async {
    val xsf = (calculator ? View).mapTo[Seq[_]]
    xsf map {
      case xs => Ok(s"$name: calculator has the no elements (starting with top): $xs")
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
