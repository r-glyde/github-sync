package github.sync.model

import cats.syntax.either._

final case class Repository(owner: String, name: String) {
  val fullname: String = s"$owner/$name"
}

object Repository {
  def fromString(str: String): Either[String, Repository] = str.split("/") match {
    case Array(owner, name) => Repository(owner, name).asRight
    case _                  => s"$str is not a valid repository".asLeft
  }
}
