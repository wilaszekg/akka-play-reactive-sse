package performance

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp

import scala.concurrent.duration.{FiniteDuration, _}

case class TestResult(received: Int)

class Tester(chatRooms: Int, chatShard: ActorRef, subscribers: Int, timeout: FiniteDuration) extends Actor {


  var waitForUp = true
  Cluster(context.system).subscribe(self, classOf[MemberUp])


  var finished = 0
  var received = 0

  override def receive: Receive = {
    case _: MemberUp if waitForUp =>
      waitForUp = false
      println("^^^^^^^^^^^^ starting TEST")
      0 until chatRooms foreach { id =>
        println(s"create sender with id $id")
        context.actorOf(Sender.props(chatShard, id.toString, subscribers / 3, subscribers, timeout))
      }
    case TestResult(r) =>
      finished += 1
      received += r
      if (finished == chatRooms) {
        println(s"***************** ALL messages: $received")
        context.system.terminate()
      }
  }
}

object Tester {
  def props(chatRooms: Int, chatShard: ActorRef, subscribers: Int, timeout: FiniteDuration) =
    Props(new Tester(chatRooms, chatShard, subscribers, timeout))
}