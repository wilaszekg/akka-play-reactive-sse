package chat

import akka.actor.{ActorLogging, Props}
import akka.pubsub.PersistentSubscriber
import api.chat.ChatRoom
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.Json

class ChatRoomsSubscriber(channel: Concurrent.Channel[String], userId: String) extends PersistentSubscriber with ActorLogging {
  override def persistenceId: String = "RoomsRepository"

  override def viewId: String = "RoomsRepository-view-" + userId

  override def subscribe: Receive = {
    case cr: ChatRoom =>
      channel.push(Json.obj("id" -> cr.id, "name" -> cr.name).toString())
  }
}

object ChatRoomsSubscriber {
  def props(channel: Concurrent.Channel[String], userId: String) = Props(new ChatRoomsSubscriber(channel, userId))
}
