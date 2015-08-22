package performance

import akka.actor._
import akka.cluster.sharding.ClusterSharding
import api.cluster.ChatShard
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.language.postfixOps

object Performance extends App {

  // set min-nr-of-members to run tests
  val port = args match {
    case Array() => "0"
    case Array(port) => port
    case args => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
  }

  System.setProperty("akka.cluster.roles.0", "frontend")
  val properties = Map(
    "akka.remote.netty.tcp.port" -> port
  )

  val system = ActorSystem("application", (ConfigFactory parseMap properties)
    .withFallback(ConfigFactory.load())
  )

  val chatShard = ClusterSharding(system).startProxy(ChatShard.name, Some("backend"), ChatShard.extractChatId, ChatShard.extractShardId)

  val chatRooms = 30
  val subscribers = 100
  system.actorOf(Tester.props(chatRooms, chatShard, subscribers, 5 seconds))


  system.awaitTermination()
}
