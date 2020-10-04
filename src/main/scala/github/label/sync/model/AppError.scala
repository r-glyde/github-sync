package github.label.sync.model

final case class AppError(msg: String) extends Exception(msg)
