import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.util.control.Breaks.{break, breakable}

object Server extends App {
  val config = ConfigFactory.load()
  val actorSystem = ActorSystem("server", config.getConfig("server").withFallback(config))
  val materializer = ActorMaterializer.create(actorSystem)

  actorSystem.actorOf(Props(classOf[OrderActor]), "order")
  actorSystem.actorOf(Props(classOf[SearchActor]), "search")
  actorSystem.actorOf(Props(classOf[StreamActor]), "stream")

  println("+ ----\033[1;33m SERVER \033[0m---- +")
  println("|\033[33m q. Exit          \033[0m|")
  println("+ ---------------- +")

  println("\033[33mServer working...\033[0m")

  breakable {
    while (true) {
      print("\033[33m>> \033[0m")
      val cmd = scala.io.StdIn.readLine()

      if (List("q", "quit", "exit", ":q", ":quit").contains(cmd))
        break
    }
  }

  actorSystem.terminate()
}
