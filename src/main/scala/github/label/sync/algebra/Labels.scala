package github.label.sync.algebra

import cats.effect.Sync
import cats.syntax.all._
import github.label.sync.model.{AppError, Label, Repository}
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

final class LiveLabels[F[_] : Sync](client: Client[F], baseUrl: Uri) extends Labels[F] {
  override def getAll(repo: Repository): F[List[Label]] =
    client.get(baseUrl / "repos" / repo.owner / repo.name / "labels") { res =>
      if (res.status.isSuccess) res.as[List[Label]]
      else Sync[F].raiseError(AppError(s"Failed to get labels in '${repo.fullname}': ${res.status.reason}"))
    }

  override def create(repo: Repository, label: Label): F[Unit] =
    Request[F](Method.POST)
      .withUri(baseUrl / "repos" / repo.owner / repo.name / "labels")
      .withEntity(label)
      .pipe(client.status)
      .flatMap(handleError(_, repo, label.name, "create"))

  override def update(repo: Repository, label: Label): F[Unit] =
    Request[F](Method.PATCH)
      .withUri(baseUrl / "repos" / repo.owner / repo.name / "labels" / label.name)
      .withEntity(label)
      .pipe(client.status)
      .flatMap(handleError(_, repo, label.name, "update"))

  override def delete(repo: Repository, name: String): F[Unit] =
    Request[F](Method.DELETE)
      .withUri(baseUrl / "repos" / repo.owner / repo.name / "labels" / name)
      .pipe(client.status)
      .flatMap(handleError(_, repo, name, "delete"))

  private def handleError(status: Status, repo: Repository, label: String, mode: String): F[Unit] =
    if (status.isSuccess) Sync[F].unit
    else Sync[F].raiseError(AppError(s"Failed to $mode '$label' in '${repo.fullname}': ${status.reason}"))
}
