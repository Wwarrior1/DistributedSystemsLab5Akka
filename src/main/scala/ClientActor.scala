import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, OneForOneStrategy, SupervisorStrategy}

import scala.concurrent.duration._

class ClientActor extends Actor {
  override def receive: Receive = {
    case Ordered =>
      println("\033[33m:: Book was ordered\033[0m")
    case Searched(titles, prices) =>
      for (i <- titles.indices)
        println("\033[33m:: " + s"'${titles(i)}' costs ${prices(i)} PLN" + "\033[0m")
    case SearchError(message) =>
      println("\033[33m:: Search failed in database: " + message + "\033[0m")
    case StreamLine(line) =>
      println("\033[37m" + line + "\033[0m")
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(10, Duration(60, "seconds")) {
    case _: Exception => Resume
  }
}
