import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class SearchActor extends Actor {
  var found = false
  implicit val timeout = Timeout(Duration(5, "seconds"))

  import context.dispatcher // All MessageDispatcher implementations are also an ExecutionContext, which means
  // that they can be used to execute arbitrary code, for instance Futures.

  override def receive: Receive = {
    case Search(title) =>
      val database1FileName = "database_1.txt"
      val database2FileName = "database_2.txt"
      val currentSender = sender
      found = false

      // Create child Actors
      val database1 = context.actorOf(Props(classOf[SearchDatabaseActor], database1FileName))
      val database2 = context.actorOf(Props(classOf[SearchDatabaseActor], database2FileName))

      // Search
      val database1Future = database1 ? Search(title)
      val database2Future = database2 ? Search(title)

      // Callbacks - on complete
      database1Future.onComplete {
        case Success(result) => handleSearch(currentSender, result)
        case Failure(result) => currentSender ! SearchError(database1FileName)
      }

      database2Future.onComplete {
        case Success(result) => handleSearch(currentSender, result)
        case Failure(result) => currentSender ! SearchError(database2FileName)
      }
  }

  private def handleSearch(currentSender: ActorRef, result: Any) = {
    val searchResponse: SearchResponse = result.asInstanceOf[SearchResponse]

    found.synchronized {
      if (!found) {
        if (searchResponse.books.length > 0)
          found = true

        var titles = Array[String]()
        var costs = Array[String]()
        for (book <- searchResponse.books) {
          titles = titles :+ book.split(";")(0)
          costs = costs :+ book.split(";")(1)
        }

        currentSender ! Searched(titles, costs)
      }
    }
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(10, Duration(60, "seconds")) {
      case _: Exception => Restart
    }
}

case class Search(title: String)
case class Searched(listOfTitles: Array[String], listOfCosts: Array[String])
case class SearchError(fileName: String)