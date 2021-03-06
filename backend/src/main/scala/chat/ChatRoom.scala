package chat

import akka.pubsub.{DirectPublisher, TriggeringPublisher}
import api.chat.ChatMessage

class ChatRoom extends DirectPublisher {

  val chatId = context.self.path.name
  override def persistenceId: String = "Chat" + chatId
  log.debug(s"Starting chat with id: $chatId")

  override def receiveCommand: Receive = {
    case msg: ChatMessage =>
      log.info(s"Chat message $msg")
      publish(msg)
  }
}
