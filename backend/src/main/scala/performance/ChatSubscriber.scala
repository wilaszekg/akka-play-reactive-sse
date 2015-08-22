package performance

import akka.actor.Props
import akka.pubsub.{PersistentSubscriber, TriggeredSubscriber, DirectSubscriber}
import api.chat.AddMessage

class ChatSubscriber(userId: String, chatId: String) extends PersistentSubscriber {
  override def viewId: String = userId + chatId

  override def persistenceId: String = "Chat" + chatId

  override def subscribe: Receive = {
    case msg: AddMessage =>
      context.parent ! MessageAck(msg.content)
  }
}

object ChatSubscriber {
  def props(userId: String, chatId: String) =
    Props(new ChatSubscriber(userId, chatId))
}