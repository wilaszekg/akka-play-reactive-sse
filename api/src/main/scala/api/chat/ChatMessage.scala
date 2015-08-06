package api.chat

trait ChatMessage {
  def chatId: String
}

case class AddMessage(chatId: String, content: String) extends ChatMessage

