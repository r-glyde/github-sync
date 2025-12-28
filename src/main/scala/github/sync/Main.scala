package github.sync

import cats.data.Validated
import cats.syntax.all._
import cats.effect.{ExitCode, IO, IOApp}
import com.monovore.decline.effect.CommandIOApp
import com.monovore.decline.{Command, Opts}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.middleware.Logger

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    CommandIOApp
      .run[IO](Command("github-sync", "synchronise labels between github repositories", helpFlag = true)(main), args)

  private def main: Opts[IO[ExitCode]] =
    Config.opts.map { case Config(token, source, targets, deleteAdditional, dryRun, isVerbose) =>
      EmberClientBuilder.default[IO].build.use { client =>
        val configuredClient = if (isVerbose) Logger(logHeaders = false, logBody = false)(client) else client
        val github           = new GithubClient[IO](configuredClient, token)
        val program          = new SyncProgram[IO](github)

        targets
          .traverse(program.syncLabels(source, _, deleteAdditional, dryRun).attempt.map(_.toValidatedNec))
          .map(_.sequence)
          .flatMap {
            case Validated.Valid(_)        => ExitCode.Success.pure[IO]
            case Validated.Invalid(errors) =>
              errors
                .traverse(e => Printer[IO].putStrLn(s"Something went wrong: ${e.getMessage}"))
                .as(ExitCode.Error)
          }
      }
    }

}
