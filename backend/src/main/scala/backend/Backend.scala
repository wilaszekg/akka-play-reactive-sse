package backend

import akka.actor._
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import api.cluster.ChatShard
import chat.{ChatRoom, RoomsRepository}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

object Backend extends App {

  // Simple cli parsing
  val port = args match {
    case Array() => "0"
    case Array(port) => port
    case args => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
  }

  System.setProperty("akka.cluster.roles.0", "backend")
  val properties = Map(
    "akka.remote.netty.tcp.port" -> port
  )

  val system = ActorSystem("application", (ConfigFactory parseMap properties)
    .withFallback(ConfigFactory.load())
  )

  system.actorOf(props = ClusterSingletonManager.props(
    singletonProps = Props(new RoomsRepository),
    terminationMessage = PoisonPill,
    settings = ClusterSingletonManagerSettings(system).withRole("backend").withSingletonName("roomsRepository")
  ),
    name = "roomsRepositorySingleton")

  ClusterSharding(system).start(
    ChatShard.name,
    Props[ChatRoom],
    ClusterShardingSettings(system).withRole("backend"),
    ChatShard.extractChatId,
    ChatShard.extractShardId)


  system.awaitTermination()
}
