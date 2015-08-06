package chat

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import api.chat.{ChatRoom, CreateRoom}

class RoomsRepository extends PersistentActor with ActorLogging {

  override def persistenceId: String = "RoomsRepository"

  var counter: Long = 0

  override def receiveRecover: Receive = {
    case ChatRoom(id, _) => counter = id
  }

  override def receiveCommand: Receive = {
    case CreateRoom(name) =>
      counter += 1
      persist(ChatRoom(counter, name)) { e => log.info("Persisted {}", e) }
    case x => log.debug("unknown message: {}", x)
  }
}
