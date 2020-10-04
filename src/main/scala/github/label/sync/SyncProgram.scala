package github.label.sync

import cats.Parallel
import cats.effect.Sync
import cats.syntax.all._
import github.label.sync.model.Repository

class SyncProgram[F[_] : Sync : Printer : Parallel](github: Github[F]) {

  def syncLabels(source: Repository, target: Repository, deleteAdditional: Boolean, dryRun: Boolean): F[Unit] =
    for {
      sourceLabels <- github.labels.getAll(source)
      targetLabels <- github.labels.getAll(target)
      tasks        = generateTasks(sourceLabels, targetLabels, deleteAdditional)
      _            <- Printer[F].putStrLn(tasks.show)
      _ <- if (dryRun) Printer[F].putStrLn("No actions taken")
          else
            for {
              _ <- tasks.toCreate.parTraverse_(label => github.labels.create(target, label))
              _ <- tasks.toUpdate.parTraverse_(label => github.labels.update(target, label))
              _ <- tasks.toDelete.parTraverse_(name => github.labels.delete(target, name))
            } yield ()
    } yield ()

}
