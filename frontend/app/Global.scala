import akka.actor.ActorSystem
import chat.Unsubscribe
import chat.controllers.ChatController
import play.api.GlobalSettings
import play.api.libs.concurrent.Akka
import play.api.mvc.{RequestHeader, WithFilters}
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter(shouldGzip =
  (request, response) => {
    val contentType = response.headers.get("Content-Type")
    contentType.exists(_.startsWith("text/html")) || request.path.endsWith("jsroutes.js")
  }
)) with GlobalSettings {
  
  override def onStart(app: play.api.Application) {
    val system: ActorSystem = Akka.system(app)
  }

  override def onRequestCompletion(request: RequestHeader): Unit = {
    request.headers.get("Accept").filter(_ == "text/event-stream").foreach { _ =>
      ChatController.subscriptionCoordinator ! Unsubscribe(request.id.toString)
    }
  }

}
