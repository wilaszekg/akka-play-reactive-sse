package akka.pubsub

trait DurablePublisher {
  def publish(event : Any): Unit
}
