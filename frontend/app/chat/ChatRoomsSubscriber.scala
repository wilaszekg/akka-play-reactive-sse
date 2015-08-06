package chat

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentView
import api.chat.ChatRoom
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.Json
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

class ChatRoomsSubscriber(channel: Concurrent.Channel[String], userId: String) extends PersistentView with ActorLogging {
  override def persistenceId: String = "RoomsRepository"

  override def viewId: String = "RoomsRepository-view-" + userId


  override def autoUpdateInterval: FiniteDuration = 100 millis

  var fakeState = 0

  override def receive: Receive = {
    case cr: ChatRoom =>
      log.info("Received chat room {}", cr)
      channel.push(Json.obj("id" -> cr.id, "name" -> cr.name).toString())
      saveSnapshot()
    case x =>
      log.debug("Unknown message: {}", x)
  }
}

object ChatRoomsSubscriber {
  def props(channel: Concurrent.Channel[String], userId: String) = Props(new ChatRoomsSubscriber(channel, userId))
}
