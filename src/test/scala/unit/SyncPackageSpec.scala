package unit

import com.danielasfregola.randomdatagenerator.magnolia.RandomDataGenerator
import github.sync.generateTasks
import github.sync.model.Label
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SyncPackageSpec extends AnyWordSpecLike with Matchers with ScalaCheckPropertyChecks with RandomDataGenerator {

  "generateTasks" should {

    "return updates for intersection of source and target labels" in {
      forAll { (_source: List[Label], _target: List[Label], deleteAdditional: Boolean) =>
        val source = _source.distinctBy(_.name)
        val target = _target.distinctBy(_.name)

        val output          = generateTasks(source, target, deleteAdditional)
        val expectedUpdates = source.map(_.name) intersect target.map(_.name)

        output.toUpdate.map(_.name) should contain theSameElementsAs expectedUpdates
      }
    }

    "return creates for source labels not present in target" in {
      forAll { (_source: List[Label], _target: List[Label], deleteAdditional: Boolean) =>
        val source = _source.distinctBy(_.name)
        val target = _target.distinctBy(_.name)

        val output          = generateTasks(source, target, deleteAdditional)
        val expectedCreates = source.map(_.name) diff target.map(_.name)

        output.toCreate.map(_.name) should contain theSameElementsAs expectedCreates
      }
    }

    "return no deletes if deleteAdditional is false" in {
      forAll { _labels: List[Label] =>
        val labels = _labels.distinctBy(_.name)
        val output = generateTasks(List.empty, labels, deleteAdditional = false)

        output.toCreate shouldBe empty
        output.toUpdate shouldBe empty
        output.toDelete shouldBe empty
      }
    }

    "return deletes for target labels not present in source" in {
      forAll { (_source: List[Label], _target: List[Label]) =>
        val source = _source.distinctBy(_.name)
        val target = _target.distinctBy(_.name)

        val output          = generateTasks(source, target, deleteAdditional = true)
        val expectedDeletes = target.map(_.name) diff source.map(_.name)

        output.toDelete should contain theSameElementsAs expectedDeletes
      }
    }
  }

}
