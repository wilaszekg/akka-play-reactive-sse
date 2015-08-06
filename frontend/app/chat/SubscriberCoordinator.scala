package chat

import akka.actor.{ActorLogging, Actor, Props, Terminated}
import play.api.libs.iteratee.Concurrent

case class ChatSubscription(userId: String, chatId: String, channel: Concurrent.Channel[String])

class SubscriberCoordinator extends Actor with ActorLogging {

  var waitingSubscribers: Map[String, ChatSubscription] = Map()

  override def receive: Receive = {
    case cs: ChatSubscription =>
      val subscriber = context.child(cs.userId)
      subscriber.foreach { actor =>
        waitingSubscribers += (cs.userId -> cs)
        context.stop(actor)
      }
      if (subscriber.isEmpty) {
        startSubscriber(cs)
      }
    case Terminated(actor) =>
      println("killed subscriber {}", actor.path.name)
      waitingSubscribers.get(actor.path.name) foreach { subscription =>
        startSubscriber(subscription)
      }
  }

  def startSubscriber(subscription: ChatSubscription) = {
    println("creating subscriber for chat: {}", subscription.chatId)
    val subscriber =
      context.actorOf(ChatSubscriber.props(subscription.channel, subscription.userId, subscription.chatId), subscription.userId)
    context.watch(subscriber)
  }
}

object SubscriberCoordinator {
  def props = Props[SubscriberCoordinator]
}
