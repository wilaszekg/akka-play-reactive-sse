package chat

import akka.actor._

case class Subscribe(subscriptionId: String, subscriberProps: Props)

case class Unsubscribe(subscriptionId: String)

class SubscriberCoordinator extends Actor with ActorLogging {

  override def receive: Receive = {
    case Subscribe(subscriptionId, props) =>
      log.debug("creating subscriber with id: {}", subscriptionId)
      context.actorOf(props, subscriptionId)

    case Unsubscribe(subscriptionId) =>
      val subscriber = context.child(subscriptionId)
      subscriber foreach {
        log.info("Unsubscribe : {}", subscriptionId)
        _ ! PoisonPill
      }
      if (subscriber.isEmpty) {
        log.warning("Failed to unsubscribe not existing subscriber id {}", subscriptionId)
      }
  }

}

object SubscriberCoordinator {
  def props = Props[SubscriberCoordinator]
}
