import akka.NotUsed
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, OneForOneStrategy, SupervisorStrategy}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy, ThrottleMode}

import scala.concurrent.duration.Duration
import scala.io.Source.fromFile

class StreamActor extends Actor {
  override def receive: Receive = {
    case StreamBook(filename) =>
      val materializer = ActorMaterializer.create(context)  // Materializing and running a stream always requires a Materializer to be in implicit scope.
      val sink = Source
        .actorRef(1000, OverflowStrategy.dropNew)           // If the buffer is full when a new element arrives, drops the new element.
        .throttle(1, Duration(1, "seconds"), 1, ThrottleMode.shaping)   // throttle - to slow down the stream to 1 element per second.
        .to(Sink.actorRef(sender, NotUsed))                 // Sink is a set of stream processing steps that has one open input. Can be used as a Subscriber.
        .run()(materializer)
      val lines = fromFile(filename).getLines
      lines.foreach(line => sink ! StreamLine(line))
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(10, Duration(60, "seconds")) {
      case _: Exception => Restart
    }
}

case class StreamBook(fileName: String)
case class StreamLine(line: String)