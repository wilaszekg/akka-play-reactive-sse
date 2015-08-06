package api.cluster

import akka.cluster.sharding.ShardRegion
import api.chat.ChatMessage

object ChatShard {
  val name = "Chat"

  def extractChatId: ShardRegion.ExtractEntityId = {
    case msg: ChatMessage => (msg.chatId, msg)
  }

  def extractShardId: ShardRegion.ExtractShardId = {
    case msg: ChatMessage => msg.chatId.hashCode.%(10).toString
  }
}
