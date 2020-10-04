package github.label.sync

import cats.effect.IO

trait Printer[F[_]] {
  def putStrLn(str: String): F[Unit]
}

object Printer {
  def apply[F[_]](implicit printer: Printer[F]): Printer[F] = printer

  implicit val ioPrinter: Printer[IO] = str => IO(println(str))
}
