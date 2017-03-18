import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

object WebServer extends App {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()(system)
  implicit val executionContext = ExecutionContext.global

  val route = ???

  Http().bindAndHandle(route, "localhost", 8080)(materializer)

  println(s"Startup complete. Listening on port 8080")
}


