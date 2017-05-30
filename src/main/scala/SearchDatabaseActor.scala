import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, OneForOneStrategy, SupervisorStrategy}

import scala.concurrent.duration.Duration
import scala.io.Source._

class SearchDatabaseActor(filename: String) extends Actor {
  override def receive: Receive = {
    case Search(title) =>
      val lines = fromFile(filename).getLines
      sender ! SearchResponse(lines.filter(s => s.startsWith(title)).toArray)
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(10, Duration(60, "seconds")) {
      case _: Exception => Stop
    }
}

case class SearchResponse(books: Array[String])