
/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
import org.scalatestplus.play._

class IntegrationSpec extends PlaySpec with OneBrowserPerSuite with OneServerPerSuite with HtmlUnitFactory {

  "Application" should {

    "work from within a browser" in {

      go to "http://localhost:" + port

      pageSource must include ("controllers.Rational Calculator: calculator has the following elements (starting with top): Stack()")
    }
  }
}
