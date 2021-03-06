package chat

import akka.actor.Props
import akka.pubsub.{DirectSubscriber, TriggeredSubscriber}
import api.chat.AddMessage
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.Json

class ChatSubscriber(channel: Concurrent.Channel[String], userId: String, chatId: String) extends DirectSubscriber {
  override def viewId: String = userId + chatId

  override def persistenceId: String = "Chat" + chatId

  override def subscribe: Receive = {
    case msg: AddMessage =>
      channel.push(Json.obj("content" -> msg.content, "sender" -> msg.sentBy).toString())
  }
}

object ChatSubscriber {
  def props(channel: Concurrent.Channel[String], userId: String, chatId: String) =
    Props(new ChatSubscriber(channel, userId, chatId))
}