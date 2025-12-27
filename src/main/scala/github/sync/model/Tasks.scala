package github.sync.model

import cats.Show
import cats.syntax.all._

final case class Tasks(toCreate: List[Label], toUpdate: List[Label], toDelete: List[String])

object Tasks {

  implicit val show: Show[Tasks] = Show.show { tasks =>
    show"""Creating: ${tasks.toCreate.map(_.name)}
          |Updating: ${tasks.toUpdate.map(_.name)}
          |Deleting: ${tasks.toDelete}
          |""".stripMargin
  }

  private implicit def showList[T: Show]: Show[List[T]] = _.mkString_("[ ", ", ", " ]")

}
