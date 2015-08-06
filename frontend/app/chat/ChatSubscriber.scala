package chat

import akka.actor.Props
import akka.persistence.PersistentView
import api.chat.AddMessage
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.Json

import scala.concurrent.duration.{FiniteDuration, _}

class ChatSubscriber(channel: Concurrent.Channel[String], userId: String, chatId: String) extends PersistentView {
  override def viewId: String = userId + chatId

  override def persistenceId: String = "Chat" + chatId

  override def autoUpdateInterval: FiniteDuration = 100 millis

  override def receive: Receive = {
    case msg: AddMessage =>
      log.info("received {}", msg)
      channel.push(Json.obj("content" -> msg.content).toString())
      saveSnapshot()
    case x =>
      log.debug("Unknown message: {}", x)

  }
}

object ChatSubscriber {
  def props(channel: Concurrent.Channel[String], userId: String, chatId: String) =
    Props(new ChatSubscriber(channel, userId, chatId))
}