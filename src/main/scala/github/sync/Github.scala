package github.sync

import cats.effect.Async
import github.sync.algebra.{Labels, LiveLabels}
import org.http4s.Credentials.Token
import org.http4s.Headers
import org.http4s.client.{Client => Http4sClient}
import org.http4s.headers.Authorization
import org.http4s.implicits.http4sLiteralsSyntax
import org.typelevel.ci.CIString

trait Github[F[_]] {
  def labels: Labels[F]
}

class GithubClient[F[_] : Async](client: Http4sClient[F], accessToken: String) extends Github[F] {

  private val authClient = Http4sClient[F] { request =>
    client.run(request.withHeaders(request.headers ++ Headers(Authorization(Token(CIString("token"), accessToken)))))
  }

  val labels = new LiveLabels[F](authClient, uri"https://api.github.com")

}
