package codemountain.finagle

import org.jboss.netty.handler.codec.http.HttpRequest
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.finagle.http.Status.{ Ok, MethodNotAllowed }
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.builder.Server
import java.net.InetSocketAddress
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.RichHttp
import com.twitter.finagle.http.filter.ExceptionFilter
import org.jboss.netty.handler.codec.http.HttpMethod
import com.twitter.finagle.http.Method._

object Helpers {
  val methNotAllwd: PartialFunction[(HttpMethod, Request), Future[Response]] = {
    case (_, request) =>
      val response = request.response
      response.status = MethodNotAllowed
      Future.value(response)
  }

  def ~~(body: PartialFunction[(HttpMethod, Request), Future[Response]]) = {
    new Service[Request, Response] {
      def apply(request: Request): Future[Response] = {

        val pf: PartialFunction[(HttpMethod, Request), Future[Response]] = {
          body orElse methNotAllwd
        }
        pf(request.method, request)
      }
    }

  }
}

class SimpleService extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest): Future[HttpResponse] = {
    val response = new DefaultHttpResponse(HTTP_1_1, Ok)
    Future.value(response)
  }
}

class AdvancedService extends Service[Request, Response] {
  def apply(request: Request): Future[Response] = {
    (Path(request.path)) match {
      case Root / "user" / Integer(id) =>
        val response = request.response
        response.setContentString("The user id is %d\n\n" format id)
        Future.value(response)
    }
  }
}

object Simple {

  def main(args: Array[String]) {
    val service = new SimpleService
    val server: Server = ServerBuilder()
      .codec(new Http)
      .bindTo(new InetSocketAddress(8099))
      .name("simple")
      .build(service)
  }

}

object Advanced {
  def main(args: Array[String]) {
    val advancedService = new AdvancedService

    val serve: Server = ServerBuilder()
      .codec(new RichHttp[Request](new Http()))
      .bindTo(new InetSocketAddress(8099))
      .name("advanced")
      .build(ExceptionFilter andThen advancedService)

  }
}

object SuperAdvanced {
  import Helpers._

  val superAd = ~~ {
    case (Get, request) => Path(request.path) match {
      case Root / "user" / Integer(id) =>
        val response = request.response
        response.setContentString("The user id is %d\n\n" format id)
        Future.value(response)
    }
  }

  def main(args: Array[String]) {
    val serve: Server = ServerBuilder()
      .codec(new RichHttp[Request](new Http()))
      .bindTo(new InetSocketAddress(8099))
      .name("suprAd")
      .build(ExceptionFilter andThen superAd)
  }
}
