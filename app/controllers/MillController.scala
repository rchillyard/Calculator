package controllers

import akka.pattern.ask
import akka.util.Timeout

import javax.inject.Inject
import models.{Mill, MillCommand, NumericToken, NumericValue, Rational}
import play.api.data._
import play.api.mvc._
import services.Akka

import scala.collection.immutable
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

  // The URL to the maybeCommand.  You can call this directly from the template, but it
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
    val eventualList: Future[List[NumericToken]] = for (xs <- getStack) yield for (x <- xs) yield x
    eventualList map {
      //      case Nil => Ok(s"$name: calculator stack is empty")
      xs =>
        val millCommands: immutable.Seq[MillCommand] = xs.map(r => MillCommand(None, Success(r).toOption))
        Ok(views.html.mill(millCommands, form, postUrl))
    }
  }

  // This will be the action that handles our form post
  def millCommand(): Action[AnyContent] = Action.async {
    implicit request: MessagesRequest[AnyContent] =>

      /**
       * NOTE: this function is actually responsible for showing the current state of the Stack.
       * It's invoked whenever the fields in the input form are empty--
       * which happens immediately after the button is pushed and millCommand is invoked.
       */
      val invalidFunction: Form[ValidCommand] => Future[Result] = { formWithErrors: Form[ValidCommand] =>
        getStack.map(xs => BadRequest(views.html.mill(xs.map(r => MillCommand(None, Success(NumericValue(r)).toOption)), formWithErrors, postUrl)))
      }


      val validFunction: ValidCommand => Future[Result] = { data: ValidCommand =>
        // This is the good case, where the form was successfully parsed as a ValidCommand object.
        val command = MillCommand(maybeCommand = data.maybeCommand, maybeValue = data.maybeValue.flatMap(w => NumericValue.parse(w).toOption))
        val result: Future[(Rational, Seq[MillCommand])] = command match {
          case MillCommand(Some(w), None) => sendToCalculator(w)
          case MillCommand(_, Some(value)) => sendToCalculator(value.toString) // TODO should really only do push
          case _ => println("Illegal mill maybeCommand"); Future(Rational.NaN -> Seq[MillCommand]())
        }
        result.map { case (r, _) => Redirect(routes.MillController.millCommand()).flashing("info" -> s"Result: $r") }
      }

      val formValidationResult: Form[ValidCommand] = form.bindFromRequest()
      formValidationResult.fold(invalidFunction, validFunction)
  }

  def show(): Action[AnyContent] = Action.async(
    getStack map {
      case Nil => Ok(s"$name: calculator stack is empty")
      case xs => Ok(s"$name: calculator has elements (starting with top): ${xs.mkString(", ")}")
    })

  // NOTE: this assumes Mill uses Rational
  private def getStack: Future[List[NumericToken]] = {
    val rmf = (calculator ? actors.View).mapTo[Mill[NumericToken]]
    for (rm <- rmf) yield for (r <- rm.toList) yield r
  }

  // NOTE: this assumes Mill uses Rational
  def sendToCalculator(s: String): Future[(NumericToken, Seq[MillCommand])] = flatten {
    (calculator ? s).mapTo[Try[NumericToken]] flatMap {
      case Success(x) =>
        getStack map {
          xs =>
            val millCommands: Seq[MillCommand] = xs.map(r => MillCommand(None, Try(r).toOption))
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
