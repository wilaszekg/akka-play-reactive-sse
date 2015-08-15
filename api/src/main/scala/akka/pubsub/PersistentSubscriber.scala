package akka.pubsub

import akka.persistence.PersistentView

import scala.concurrent.duration._
import scala.language.postfixOps

trait PersistentSubscriber extends PersistentView with DurableSubscriber {

  override def autoUpdateInterval: FiniteDuration = 100 millis
  var fakeState = 0

  override def receive: Receive = subscribe
    .andThen(handleEvent)
    .orElse { case msg => log.info("Unhandled: {}", msg) }

  private def handleEvent(event: Any) = {
    log.info("Persistent subscriber handled event {}", event)
    saveSnapshot(fakeState)
  }

}
