package chat.controllers

import java.util.UUID

import akka.cluster.sharding.ClusterSharding
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import api.chat.{AddMessage, CreateRoom}
import api.cluster.ChatShard
import chat.{ChatSubscription, SubscriberCoordinator, ChatRoomsSubscriber, ChatSubscriber}
import play.api.data.{Form, Forms}
import play.api.libs.EventSource
import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.mvc.{Action, Controller, Request}
import play.libs.Akka

import scala.concurrent.ExecutionContext.Implicits.global

object ChatController extends Controller {

  val retry = 1000

  val roomsRepository = Akka.system().actorOf(ClusterSingletonProxy.props(
    singletonManagerPath = "/user/roomsRepositorySingleton",
    settings = ClusterSingletonProxySettings.create(Akka.system()).withRole("backend").withSingletonName("roomsRepository")),
    "roomsRepositorySingletonProxy")

  val chatShard = ClusterSharding(Akka.system()).startProxy(ChatShard.name, Some("backend"), ChatShard.extractChatId, ChatShard.extractShardId)

  val chatCoordinator = Akka.system().actorOf(SubscriberCoordinator.props)

  case class AddRoom(name: String)

  case class PostMessage(content: String)

  val addRoomForm = Form(Forms.mapping(
    "name" -> Forms.text
  )(AddRoom.apply)(AddRoom.unapply))

  val postMessageForm = Form(Forms.mapping(
    "content" -> Forms.text
  )(PostMessage.apply)(PostMessage.unapply))

  def main = Action {
    Ok(views.html.chat()).withSession("userId" -> UUID.randomUUID().toString)
  }


  def roomsFeed = Action { req =>
    val userId = sessionId(req)
    val out: Enumerator[String] = Concurrent.unicast[String](channel => {
      Akka.system().actorOf(ChatRoomsSubscriber.props(channel, userId))
    })

    val host = req.headers.get("Host").getOrElse("UNKNOWN")

    Ok.feed(Enumerator(s"retry: $retry\n", s"data: Connected to: $host \n\n") >>> (out &> EventSource()))
      .as("text/event-stream").withHeaders("Cache-Control" -> "no-cache")
  }

  def chatFeed(chatId: String) = Action { req =>
    val userId = sessionId(req)
    val out: Enumerator[String] = Concurrent.unicast[String](onStart = channel => {
      chatCoordinator ! ChatSubscription(userId, chatId, channel)
    })
    Ok.feed(Enumerator(s"retry: $retry\n") >>> (out &> EventSource()))
      .as("text/event-stream").withHeaders("Cache-Control" -> "no-cache")
    /*.as( "text/event-stream")
        .withHeaders("Content-Type"->"text/event-stream")
        .withHeaders("Cache-Control"->"no-cache")
        .withHeaders("Connection"->"keep-alive")*/
  }

  def addRoom = Action { implicit req =>
    val roomForm = addRoomForm.bindFromRequest.get
    roomsRepository ! CreateRoom(roomForm.name)
    Ok
  }

  def postChatMessage(chatId: String) = Action { implicit req =>
    val messageForm = postMessageForm.bindFromRequest.get
    chatShard ! AddMessage(chatId, messageForm.content)
    Ok
  }

  private def sessionId(request: Request[_]) = request.session.get("userId").get

}
