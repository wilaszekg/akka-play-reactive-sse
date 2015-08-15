package akka.pubsub

import akka.actor.Actor.Receive

trait DurableSubscriber {
  def subscribe: Receive
}
