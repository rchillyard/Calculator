package controllers

import javax.inject.Inject
import models.MillCommand
import play.api.data._
import play.api.mvc._

import scala.collection._

/**
 * The classic MillController using MessagesAbstractController.
 *
 * Instead of MessagesAbstractController, you can use the I18nSupport trait,
 * which provides implicits that create a Messages instance from a request
 * using implicit conversion.
 *
 * See https://www.playframework.com/documentation/2.8.x/ScalaForms#passing-messagesprovider-to-form-helpers
 * for details.
 */
class MillController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  import MillForm._

  private val commands = mutable.ArrayBuffer[MillCommand]()

  // The URL to the command.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "MillController" references are inside the .scala file.
  private val postUrl = routes.MillController.millCommand()

  def index: Action[AnyContent] = Action {
    Ok(views.html.index())
  }

  def showMill: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.listWidgets(commands.toSeq, form, postUrl))
  }

  // This will be the action that handles our form post
  def millCommand: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.listWidgets(commands.toSeq, formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      // This is the good case, where the form was successfully parsed as a Data object.
      val command = MillCommand(command = data.command, value = data.value)
      commands += command
      Redirect(routes.MillController.showMill()).flashing("info" -> "Command/value added!")
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
