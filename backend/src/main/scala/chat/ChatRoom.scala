package chat

import akka.pubsub.TriggeringPublisher
import api.chat.ChatMessage

class ChatRoom extends TriggeringPublisher {

  val chatId = context.self.path.name
  override def persistenceId: String = "Chat" + chatId
  log.debug(s"Starting chat with id: $chatId")

  override def receiveCommand: Receive = {
    case msg: ChatMessage => publish(msg)
  }
}
