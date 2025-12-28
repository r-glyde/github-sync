package github

import github.sync.model.{Label, Tasks}
import org.http4s.util.CaseInsensitiveString

package object sync {

  def generateTasks(sourceLabels: List[Label], targetLabels: List[Label], deleteAdditional: Boolean): Tasks = {
    val sourceByName = sourceLabels.map(label => label.name -> label).toMap
    val targetByName = targetLabels.map(label => label.name -> label).toMap

    val toCreate = sourceByName -- targetByName.keySet
    val toUpdate = sourceByName -- toCreate.keySet
    val toDelete = if (deleteAdditional) (targetByName.keySet -- sourceByName.keySet).toList else List.empty

    Tasks(toCreate.values.toList, toUpdate.values.toList, toDelete.map(_.toString))
  }
}
