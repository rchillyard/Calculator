package controllers

import akka.pattern.ask
import akka.util.Timeout
import javax.inject.Inject
import models.{Mill, MillCommand, Rational}
import play.api.data._
import play.api.mvc._
import services.Akka

import scala.collection._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
 * The classic MillController using MessagesAbstractController.
 *
 * Instead of MessagesAbstractController, you can use the I18nSupport trait,
 * which provides implicits that create a Messages instance from a request
 * using implicit conversion.
 *
 * See https://www.playframework.com/documentation/2.8.x/ScalaForms#passing-messagesprovider-to-form-helpers
 * for details.
 *
 * NOTE: please note that this is nowhere near to being good code ;)
 */
class MillController @Inject()(akka: Akka, cc: MessagesControllerComponents)(implicit assetsFinder: AssetsFinder, ec: ExecutionContext) extends MessagesAbstractController(cc) {

  import MillForm._

  // CONSIDER surely we shouldn't have variable state in a controller??
  // NOTE: this isn't really a list of MillCommands: it's now what's on the stack.
  // TODO rework this.
  private val commands = mutable.ArrayBuffer[MillCommand]()

  // The URL to the command.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "MillController" references are inside the .scala file.
  private val postUrl = routes.MillController.millCommand()

  private val calculator = akka.getCalculator
  private val name = akka.getName
  implicit val timeout: Timeout = Timeout(10.seconds)

  def index: Action[AnyContent] = Action {
    Ok(views.html.index())
  }

  def showMill: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    println("showMill")
    // Pass an unpopulated form to the template
    val eventualList: Future[List[Rational]] = getStack
    eventualList.foreach(println(_))
    eventualList map {
      //      case Nil => Ok(s"$name: calculator stack is empty")
      xs =>
        val millCommands: immutable.Seq[MillCommand] = xs.map(r => MillCommand("", Try(r.toInt).toOption))
        Ok(views.html.mill(millCommands, form, postUrl))
    }
  }

  // This will be the action that handles our form post
  def millCommand: Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>

      val errorFunction: Form[Data] => Future[Result] = { formWithErrors: Form[Data] =>
        // This is the bad case, where the form had validation errors.
        // Let's show the user the form again, with the errors highlighted.
        // Note how we pass the form with errors to the template.
        Future(BadRequest(views.html.mill(commands.toSeq, formWithErrors, postUrl)))
      }

      val successFunction: Data => Future[Result] = { data: Data =>
        // This is the good case, where the form was successfully parsed as a Data object.
        val command = MillCommand(command = data.command, value = data.value)
        commands += command
        val result: Future[(Rational, Seq[MillCommand])] = command match {
          case MillCommand(w, None) => sendToCalculator(w)
          case MillCommand(_, Some(value)) => sendToCalculator(value.toString) // TODO should really only do push
          case _ => println("Illegal mill command"); Future(Rational.NaN -> Seq[MillCommand]())
        }
        result.onComplete {
          case Success((_, xs)) =>
            commands.clear()
            xs.foreach(commands.append)
          case _ =>
        }
        result.map { case (r, xs) => Redirect(routes.MillController.millCommand()).flashing("info" -> s"Result: $r") }
        //        Future(Redirect(routes.MillController.millCommand()).flashing("info" -> "Command/value added!"))
      }

      val formValidationResult: Form[Data] = form.bindFromRequest
      formValidationResult.fold(errorFunction, successFunction)
  }

  def show(): Action[AnyContent] = Action.async(
    getStack map {
      case Nil => Ok(s"$name: calculator stack is empty")
      case xs => Ok(s"$name: calculator has elements (starting with top): ${xs.mkString(", ")}")
    })

  // NOTE: this assumes Mill uses Rational
  private def getStack: Future[List[Rational]] = (calculator ? actors.View).mapTo[Mill[Rational]].map(_.iterator.toList)

  // NOTE: this assumes Mill uses Rational
  def sendToCalculator(s: String): Future[(Rational, Seq[MillCommand])] = flatten {
    (calculator ? s).mapTo[Try[Rational]] flatMap {
      case Success(x) =>
        getStack map {
          xs =>
            commands.clear()
            val millCommands: immutable.Seq[MillCommand] = xs.map(r => MillCommand("", Try(r.toInt).toOption))
            Success(x -> millCommands)
        }
      case Failure(e) =>
        Future(if (s == "clr") Success(Rational.NaN -> Seq[MillCommand]()) else Failure(new Exception(s"""$name: you entered "$s" which caused:""", e)))
    }
  }

  def flatten[X](xyf: Future[Try[X]]): Future[X] = for (xy <- xyf; x <- asFuture(xy)) yield x

  def asFuture[X](xy: Try[X]): Future[X] = xy match {
    case Success(s) => Future.successful(s)
    case Failure(e) => Future.failed(e)
  }

}
