package controllers

object MillForm {

  import play.api.data.Form
  import play.api.data.Forms._

  /**
   * A form processing DTO that maps to the form below.
   *
   * Using a class specifically for form binding reduces the chances
   * of a parameter tampering attack and makes code clearer.
   *
   * CONSIDER can we eliminate this and simply use MillCommand?
   */
  case class ValidCommand(maybeCommand: Option[String], maybeValue: Option[String]) {
    def isValid: Boolean = maybeCommand.isDefined || maybeValue.isDefined
  }

  /**
   * The form definition for the "create a mill maybeCommand" form.
   * It specifies the form fields and their types,
   * as well as how to convert from a ValidCommand to form data and vice versa.
   */
  val form: Form[ValidCommand] = Form(
    mapping(
      "command" -> optional(text),
      "value" -> optional(text)
    )(ValidCommand.apply)(ValidCommand.unapply).verifying(_.isValid)
  )
}
