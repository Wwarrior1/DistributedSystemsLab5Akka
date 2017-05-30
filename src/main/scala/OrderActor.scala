import java.io.FileWriter

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Actor, OneForOneStrategy, SupervisorStrategy}

import scala.concurrent.duration.Duration

class OrderActor() extends Actor {
  override def receive: Receive = {
    case Order(title: String) =>
      val currentSender = sender
      orderSingleton.orderBook(title)
      currentSender ! Ordered
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(10, Duration(60, "seconds")) {
      case _: Exception => Escalate // because of other threads
    }
}

case class Order(title: String)
case class Ordered()

object orderSingleton {
  def orderBook(title: String): Unit = {
    new FileWriter("orders.txt", true) { // append to end of file
      write(s"$title\n")
      close()
    }
  }
}