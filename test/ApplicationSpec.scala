import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class ApplicationSpec extends PlaySpec with GuiceOneAppPerSuite {

  "Application" should {

    "send 404 on a bad request" in {
      val Some(home) = route(app,FakeRequest(GET, "/boum"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include ("controllers.Rational Calculator: you entered \"boum\" which caused error: java.lang.IllegalArgumentException: operator boum is not supported")

    }

    "render the index page" in {
      val Some(home) = route(app,FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include ("controllers.Rational Calculator: calculator has the following elements (starting with top): Stack()")
    }
  }

}
