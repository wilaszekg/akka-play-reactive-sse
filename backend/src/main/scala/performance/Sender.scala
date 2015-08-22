package performance

import akka.actor.{PoisonPill, ActorRef, Actor, Props}
import api.chat.AddMessage

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class MessageAck(content: String)

class Sender(chatShard: ActorRef, chatId: String, newMsgAfter: Int, subscribersQuantity: Int, timeout: FiniteDuration) extends Actor {

  import context.dispatcher

  val subscribers = (0 until subscribersQuantity) map { id => context.actorOf(ChatSubscriber.props(id.toString, chatId)) }

  var receivedAck = 0
  var currentSequenceNr = 0

  var start: Long = _
  // val statistics = ...

  override def preStart(): Unit = {
    println(s"starting sender for chat: $chatId")
    sendToAll()
  }


  override def postStop(): Unit = {
    val receivedInTime = receivedAck - subscribersQuantity
    println(s"-------- STATS [t:${System.currentTimeMillis() - start}] -----  receivedAck: $receivedAck --- seqNR: $currentSequenceNr --- recInTime: $receivedInTime")
    context.parent ! TestResult(receivedInTime)
  }

  override def receive: Receive = {
    case MessageAck(content) =>
      receivedAck += 1

      if (shouldSendNext) {
        sendToAll()
      }
  }

  private def sendToAll() = {
    chatShard ! AddMessage(chatId, currentSequenceNr.toString)

    if (currentSequenceNr == 1) {
      start = System.currentTimeMillis()
      context.system.scheduler.scheduleOnce(timeout) {
        context.stop(self)
      }
    }
    currentSequenceNr += 1
  }

  private def shouldSendNext = {
    (receivedAck >= subscribersQuantity) && ((receivedAck - subscribersQuantity) % newMsgAfter == 0)
  }
}

object Sender {
  def props(chatShard: ActorRef, chatId: String, newMsgAfter: Int, subscribersQuantity: Int, timeout: FiniteDuration) =
    Props(new Sender(chatShard, chatId, newMsgAfter, subscribersQuantity, timeout))
}