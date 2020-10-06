package github.sync

import cats.effect.{ExitCode, IO, IOApp, Resource, SyncIO}
import com.monovore.decline.effect.CommandIOApp
import com.monovore.decline.{Command, Opts}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.Logger

import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  private val ec: ExecutionContext = ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] =
    CommandIOApp
      .run[IO](Command("github-sync", "synchronise labels between github repositories", helpFlag = true)(main), args)

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] = Resource.liftF(SyncIO(ec))

  private def main: Opts[IO[ExitCode]] =
    Config.opts.map { case Config(token, source, target, deleteAdditional, dryRun, isVerbose) =>
      BlazeClientBuilder[IO](ec).resource.use { client =>
        val configuredClient = if (isVerbose) Logger(logHeaders = false, logBody = false)(client) else client
        val github           = new GithubClient[IO](configuredClient, token)
        val program          = new SyncProgram[IO](github)

        program
          .syncLabels(source, target, deleteAdditional, dryRun)
          .as(ExitCode.Success)
          .handleErrorWith(e => Printer[IO].putStrLn(s"Something went wrong: ${e.getMessage}") as ExitCode.Error)
      }
    }

}
