package akka.pubsub

import akka.actor.ActorLogging
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.persistence.{PersistentActor, RecoveryCompleted}

trait TriggeringPublisher extends PersistentActor with DurablePublisher with ActorLogging {
  val mediator = DistributedPubSub(context.system).mediator

  override def receiveRecover: Receive = {
    case RecoveryCompleted => log.info("Triggering publisher recovered with id {}", persistenceId)
  }

  override final def publish(event: Any) = {
    persist(event) { _ =>
      mediator ! DistributedPubSubMediator.Publish(persistenceId, TriggerUpdate)
    }
  }

}
