package services

import akka.actor.{ActorRef, ActorSystem}
import application.Application
import com.typesafe.config.ConfigFactory
import javax.inject.{Inject, _}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
 * This class demonstrates how to run code when the
 * application starts and stops. It starts a timer when the
 * application starts. When the application stops it prints out how
 * long the application was running for.
 *
 * This class is registered for Guice dependency injection in the
 * [[Module]] class. We want the class to start when the application
 * starts, so it is registered as an "eager singleton". See the code
 * in the [[Module]] class to see how this happens.
 *
 * This class needs to run code when the server stops. It uses the
 * application's [[ApplicationLifecycle]] to register a stop hook.
 *
 * TODO add logger service as parameter
 */
@Singleton
class Akka @Inject()(appLifecycle: ApplicationLifecycle) {

  // This code is called when the application starts.
  private val actorSystem = ActorSystem("Calculator")
  println(s"Akka: Started actorSystem.")

  def getActorSystem: ActorSystem = actorSystem

  private val config = ConfigFactory.load()
  implicit val system: ActorSystem = actorSystem
  private val setup = config.getString("calculator") match {
    case "rational" => Application.getSetupForRational
    case "double" => Application.getSetupForDouble
    case x => Console.err.println(s"Unsupported calculator type: $x"); Application.getSetupForRational
  }
  private val calculator = system.actorOf(setup._1, setup._2)
  println(s"Akka: Calculator actor started with props ${setup._1} with name ${setup._2}") // TODO send this to logs

  private val name: String = setup._3;
  println(s"Akka: $name is ready") // TODO send this to logs

  def getCalculator: ActorRef = calculator

  def getName: String = name

  // When the application starts, register a stop hook with the
  // ApplicationLifecycle object. The code inside the stop hook will
  // be run when the application stops.
  appLifecycle.addStopHook { () =>
    println(s"Akka: Stopping application.")
    Future.successful(())
  }
}