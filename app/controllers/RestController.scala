package controllers

import actors._
import akka.pattern.ask
import akka.util.Timeout
import application.Application
import com.typesafe.config.ConfigFactory
import javax.inject._
import models._
import play.api.mvc._
import services.Akka

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RestController @Inject()(akka: Akka, cc: ControllerComponents)(implicit assetsFinder: AssetsFinder, ec: ExecutionContext)
  extends AbstractController(cc) {

  val config = ConfigFactory.load()
  val which = config.getString("calculator")

  implicit val timeout: Timeout = Timeout(10.seconds)
  implicit val system = akka.getActorSystem
  val setup = which match {
    case "rational" => Application.getSetupForRational
    case "double" => Application.getSetupForDouble
    case _ => Console.err.println(s"Unsupported calculator type: $which"); Application.getSetupForRational
  }
  val calculator = system.actorOf(setup._1, "junk")
  val name: String = setup._3;
  println(s"$name is ready") // TODO send this to logs

  def index(): Action[AnyContent] = Action.async(
    (calculator ? View).mapTo[Mill[_]].map(_.iterator.toList) map {
      case Nil => Ok(s"$name: calculator stack is empty")
      case xs => Ok(s"$name: calculator has elements (starting with top): ${xs.mkString(", ")}")
    }
  )

  def command(s: String): Action[AnyContent] = Action.async {
    (calculator ? s).mapTo[Try[_]] map {
      case Success(x) => Ok(s"""$name: you entered "$s" and got back $x""")
            case Failure(e) => if (s=="clr") Redirect(routes.RestController.index) else  Ok(s"""$name: you entered "$s" which caused error: $e""")
    }
  }
}
