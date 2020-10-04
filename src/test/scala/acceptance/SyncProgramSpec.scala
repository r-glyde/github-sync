package acceptance

import cats.effect.{ContextShift, IO}
import github.label.sync.algebra.Labels
import github.label.sync.model.{Label, Repository}
import github.label.sync.{Github, Printer, SyncProgram}
import org.scalatest.FutureOutcome
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.FixtureAsyncWordSpecLike

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

// TODO: everything in here could probably be property tested
class SyncProgramSpec extends FixtureAsyncWordSpecLike with Matchers with TableDrivenPropertyChecks {

  override implicit val executionContext: ExecutionContext = ExecutionContext.global
  implicit val contextShift: ContextShift[IO]              = IO.contextShift(executionContext)

  type FixtureParam = TestContext

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = test { new TestContext() }

  val source: Repository = Repository("test", "source")
  val target: Repository = Repository("test", "target")

  "syncLabels" should {

    "run tasks to synchronise source and target labels" in { ctx =>
      import ctx._
      val program = new SyncProgram[IO](github)

      program
        .syncLabels(source, target, deleteAdditional = true, dryRun = false)
        .map { _ =>
          creates shouldBe List(Label("b", "red", None))
          updates shouldBe List(Label("a", "blue", None))
          deletes shouldBe List("c")
          printed should not contain "No actions taken"
        }
        .unsafeToFuture()
    }

    "do nothing  if dryRun is set to false" in { ctx =>
      import ctx._
      val program = new SyncProgram[IO](github)

      program
        .syncLabels(source, target, deleteAdditional = true, dryRun = true)
        .map { _ =>
          creates shouldBe empty
          updates shouldBe empty
          deletes shouldBe empty
          printed.last shouldBe "No actions taken"
        }
        .unsafeToFuture()
    }

    "do nothing if source or target repo does not exist" in { ctx =>
      import ctx._
      val repos = Table(
        ("source", "target"),
        (source, Repository("test", "wrong")),
        (Repository("test", "wrong"), target)
      )

      forAll(repos) { (sourceRepo, targetRepo) =>
        val program = new SyncProgram[IO](github)

        recoverToExceptionIf[Exception] {
          program.syncLabels(sourceRepo, targetRepo, deleteAdditional = true, dryRun = false).unsafeToFuture()
        }.map { _ =>
          creates shouldBe empty
          updates shouldBe empty
          deletes shouldBe empty
          printed shouldBe empty
        }
      }
    }
  }

  class TestContext {
    private val sourceLabels: List[Label] = List(Label("a", "blue", None), Label("b", "red", None))
    private val targetLabels: List[Label] = List(Label("a", "yellow", None), Label("c", "green", None))

    val printed: ListBuffer[String] = ListBuffer.empty
    val creates: ListBuffer[Label]  = ListBuffer.empty
    val updates: ListBuffer[Label]  = ListBuffer.empty
    val deletes: ListBuffer[String] = ListBuffer.empty

    implicit val testPrinter: Printer[IO] = str => IO { printed += str }.void

    val github: Github[IO] = new Github[IO] {
      override val labels: Labels[IO] = new Labels[IO] {
        override def getAll(repo: Repository): IO[List[Label]] = repo match {
          case `source` => IO.pure(sourceLabels)
          case `target` => IO.pure(targetLabels)
          case _        => IO.raiseError(new Exception(s"$repo not found"))
        }
        override def create(repo: Repository, label: Label): IO[Unit] = IO { creates += label }.void
        override def update(repo: Repository, label: Label): IO[Unit] = IO { updates += label }.void
        override def delete(repo: Repository, name: String): IO[Unit] = IO { deletes += name }.void
      }
    }
  }
}
