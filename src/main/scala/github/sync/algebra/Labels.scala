package github.sync.algebra

import cats.effect.Async
import cats.syntax.all._
import github.sync.model.{AppError, Label, Repository}
import org.http4s.{Method, Request, Status, Uri}
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec._

import scala.util.chaining._

trait Labels[F[_]] {
  def getAll(repo: Repository): F[List[Label]]
  def create(repo: Repository, label: Label): F[Unit]
  def update(repo: Repository, label: Label): F[Unit]
  def delete(repo: Repository, name: String): F[Unit]
}

final class LiveLabels[F[_] : Async](client: Client[F], baseUrl: Uri) extends Labels[F] {

  private val labelsUri = (repo: Repository) => baseUrl / "repos" / repo.owner / repo.name / "labels"

  override def getAll(repo: Repository): F[List[Label]] =
    client.get(labelsUri(repo)) { res =>
      if (res.status.isSuccess) res.as[List[Label]]
      else Async[F].raiseError(AppError(s"Failed to get labels in '${repo.fullname}': ${res.status.reason}"))
    }

  override def create(repo: Repository, label: Label): F[Unit] =
    Request[F](Method.POST)
      .withUri(labelsUri(repo))
      .withEntity(label)
      .pipe(client.status)
      .flatMap(handleError(_, repo, label.name.toString, "create"))

  override def update(repo: Repository, label: Label): F[Unit] =
    Request[F](Method.PATCH)
      .withUri(labelsUri(repo) / label.name.toString)
      .withEntity(label)
      .pipe(client.status)
      .flatMap(handleError(_, repo, label.name.toString, "update"))

  override def delete(repo: Repository, name: String): F[Unit] =
    Request[F](Method.DELETE)
      .withUri(labelsUri(repo) / name)
      .pipe(client.status)
      .flatMap(handleError(_, repo, name, "delete"))

  private def handleError(status: Status, repo: Repository, label: String, mode: String): F[Unit] =
    if (status.isSuccess) Async[F].unit
    else Async[F].raiseError(AppError(s"Failed to $mode '$label' in '${repo.fullname}': ${status.reason}"))
}
