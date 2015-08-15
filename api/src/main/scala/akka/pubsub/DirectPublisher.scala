package akka.pubsub

import akka.actor.ActorLogging
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.persistence.{RecoveryCompleted, PersistentActor}

trait DirectPublisher extends DurablePublisher with PersistentActor with ActorLogging {
  val mediator = DistributedPubSub(context.system).mediator

  override def receiveRecover: Receive = {
    case RecoveryCompleted => log.info("Direct publisher recovered with id {}", persistenceId)
  }

  override final def publish(event: Any) = {
    persist(event) { e =>
      mediator ! DistributedPubSubMediator.Publish(persistenceId, DirectEvent(e, lastSequenceNr))
    }
  }

}
