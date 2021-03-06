package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Terminated
import domain.{SubscribeMessage, Message}
import play.libs.Akka
import akka.actor.Props

import scala.collection.mutable.ListBuffer

class SupervisorActor extends Actor with ActorLogging {
  var users = Set[ActorRef]()
  var history = ListBuffer[Message]()

  def receive = LoggingReceive {
    case m: Message => {
        val transformedMessage = transform(m)
        users filter(ref => ref != sender) map(_ ! transformedMessage)
        history += transformedMessage
    }
    case SubscribeMessage =>
      users += sender
      context watch sender
      history map(message => sender ! message)
    case Terminated(user) => users -= user
  }

  def transform(message: Message): Message = message
}

object SupervisorActor {
  lazy val supervisor = Akka.system().actorOf(Props[SupervisorActor])

  def apply() = supervisor
}