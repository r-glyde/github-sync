package github.sync

import cats.data.Validated.{invalidNel, valid}
import cats.syntax.all._
import com.monovore.decline.Opts
import github.sync.model.Repository

final case class Config(
    token: String,
    source: Repository,
    target: Repository,
    deleteAdditional: Boolean,
    dryRun: Boolean,
    verbose: Boolean
)

object Config {
  private val tokenOpts: Opts[String] =
    Opts.option[String](
      long = "token",
      help = s"personal access token with permissions for source and target repositories"
    )

  private val repoOpts: String => Opts[Repository] = str =>
    Opts
      .option[String](metavar = "repository", long = str, help = s"$str repository as owner/repo")
      .mapValidated(Repository.fromString(_) match {
        case Right(repo) => valid(repo)
        case Left(error) => invalidNel(error)
      })

  private val deleteAdditional: Opts[Boolean] =
    Opts.flag(long = "delete", help = "delete additional labels not found in source repository").orFalse

  private val dryRun: Opts[Boolean] =
    Opts.flag(long = "dry-run", help = "only log actions to be taken").orFalse

  private val verbosity: Opts[Boolean] =
    Opts.flag(long = "verbose", help = "show logging of http requests").orFalse

  val opts: Opts[Config] =
    (tokenOpts, repoOpts("source"), repoOpts("target"), deleteAdditional, dryRun, verbosity).mapN(Config.apply)

}
