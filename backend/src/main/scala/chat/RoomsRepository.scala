package chat

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import akka.pubsub.PersistingPublisher
import api.chat.{ChatRoom, CreateRoom}

class RoomsRepository extends PersistingPublisher {

  override def persistenceId: String = "RoomsRepository"

  var counter: Long = 0

  override def receiveRecover: Receive = {
    case ChatRoom(id, _) => counter = id
  }

  override def receiveCommand: Receive = {
    case CreateRoom(name) =>
      counter += 1
      publish(ChatRoom(counter, name))
    case x => log.debug("unknown message: {}", x)
  }
}
