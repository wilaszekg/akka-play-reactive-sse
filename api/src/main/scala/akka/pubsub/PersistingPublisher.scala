package akka.pubsub

import akka.actor.ActorLogging
import akka.persistence.{RecoveryCompleted, PersistentActor}

trait PersistingPublisher extends PersistentActor with DurablePublisher with ActorLogging {

  override def receiveRecover: Receive = {
    case RecoveryCompleted => log.info("Triggering publisher recovered with id {}", persistenceId)
  }

  override final def publish(event: Any) = {
    persist(event) { e =>
      log.debug("Event persisted: {}", e)
    }
  }

}
