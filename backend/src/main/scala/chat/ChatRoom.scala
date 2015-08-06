package chat

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import api.chat.ChatMessage

class ChatRoom extends PersistentActor with ActorLogging {

  val chatId = context.self.path.name

  log.debug(s"Starting chat with id: $chatId")

  override def receiveRecover: Receive = {
    case x => log.debug("Recovering: {}", x)
  }

  override def receiveCommand: Receive = {
    case msg: ChatMessage => persist(msg) { event =>
      log.info("Persisted chat event: {}", event)
    }
  }

  override def persistenceId: String = "Chat" + chatId
}
