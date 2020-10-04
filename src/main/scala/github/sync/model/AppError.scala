package github.sync.model

final case class AppError(msg: String) extends Exception(msg)
