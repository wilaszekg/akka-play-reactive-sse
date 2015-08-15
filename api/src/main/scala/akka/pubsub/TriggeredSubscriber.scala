package akka.pubsub

import akka.cluster.pubsub.{DistributedPubSubMediator, DistributedPubSub}
import akka.persistence.{Update, PersistentView}

import scala.concurrent.duration._

case object TriggerUpdate

trait TriggeredSubscriber extends PersistentView with DurableSubscriber {

  override def autoUpdateInterval: FiniteDuration = 10 seconds

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! DistributedPubSubMediator.Subscribe(persistenceId, self)

  override def receive = subscribe
    .andThen(handleEvent)
    .orElse(handleMessage)

  private def handleMessage: Receive = {
    case TriggerUpdate => self ! Update()
    case msg => log.info("Unhandled: {}", msg)
  }

  private def handleEvent(event: Any) = {
    log.info("Triggered subscriber handled event {}", event)
    saveSnapshot()
  }

}
