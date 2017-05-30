import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.control.Breaks.{break, breakable}

object Client extends App {
  val config: Config = ConfigFactory.load()
  val actorSystem = ActorSystem("client", config.getConfig("client").withFallback(config))

  // Define actors references
  val clientActor = actorSystem.actorOf(Props(classOf[ClientActor]))
  val actorOrder = actorSystem.actorSelection("akka.tcp://server@127.0.0.1:3552/user/order")
  val actorSearch = actorSystem.actorSelection("akka.tcp://server@127.0.0.1:3552/user/search")
  val actorStream = actorSystem.actorSelection("akka.tcp://server@127.0.0.1:3552/user/stream")

  println("+ ----\033[1;33m CLIENT \033[0m---- +")
  println("|\033[33m 1. Search a book \033[0m|")
  println("|\033[33m 2. Order a book  \033[0m|")
  println("|\033[33m 3. Read a book   \033[0m|")
  println("|\033[33m q. Exit          \033[0m|")
  println("+ ---------------- +")

  breakable {
    while (true) {
      print("\033[33m>> \033[0m")
      val cmd = scala.io.StdIn.readLine()
      var title = ""

      if (List("q", "quit", "exit", ":q", ":quit").contains(cmd))
        break
      if (List("1", "2", "3").contains(cmd)) {
        print("\033[33mGive a title: \033[0m")
        title = scala.io.StdIn.readLine()
      }

      if (cmd.startsWith("1"))
        actorSearch.tell(Search(title), clientActor)
      else if (cmd.startsWith("2"))
        actorOrder.tell(Order(title), clientActor)
      else if (cmd.startsWith("3"))
        actorStream.tell(StreamBook(title), clientActor)
    }
  }

  actorSystem.terminate()
}
