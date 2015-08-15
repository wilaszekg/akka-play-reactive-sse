package akka.pubsub

import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.persistence.{SnapshotOffer, SaveSnapshotSuccess, PersistentView, Update}

import scala.concurrent.duration._
import scala.language.postfixOps

case class DirectEvent(event: Any, sequenceNr: Long)

trait DirectSubscriber extends PersistentView with DurableSubscriber {

  override def autoUpdateInterval = 20 seconds

  private var lastDirectEventSequenceNr: Long = 0L

  var fakeState = 0

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! DistributedPubSubMediator.Subscribe(persistenceId, self)

  override def snapshotSequenceNr: Long = {
    lastDirectEventSequenceNr max lastSequenceNr
  }

  override def lastSequenceNr: Long = {
    super.lastSequenceNr max lastDirectEventSequenceNr
  }

  override def receive: Receive = {
    case DirectEvent(event, sequenceNr) =>
      log.info("Received direct event: {} with sequenceNr {}", event, sequenceNr)
      if (sequenceNr == snapshotSequenceNr + 1)
        inOrderEvent(event, sequenceNr)
      else
        self ! Update(await = true)

    case sss: SaveSnapshotSuccess =>
      log.debug("Save snap success {}", sss)
    case so: SnapshotOffer =>
      log.debug("Snapshot offer {}", so)
    case ack: SubscribeAck =>
      log.debug("Subscribe ACK {}", ack)

    case event => subscribe.andThen { _ =>
      log.info("Event {} replayed with sequenceId {}", event, lastSequenceNr)
      saveSnapshot(fakeState)
    }.orElse(unhandledMessage)(event)
  }

  private def inOrderEvent(event: Any, sequenceNr: Long) = {
    log.info("In order event: {}", event)
    lastDirectEventSequenceNr = sequenceNr
    subscribe.orElse(unhandledEvent)(event)
    saveSnapshot(fakeState)
  }

  private def unhandledEvent: Receive = {
    case x => log.warning("Unhandled in order event: {}", x)
  }

  private def unhandledMessage: Receive = {
    case x => log.info("Message not handled by subscriber: {}", x)
  }

}
