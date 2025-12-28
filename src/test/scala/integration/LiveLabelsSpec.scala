package integration

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import github.sync.algebra.LiveLabels
import github.sync.model.{AppError, Label, Repository}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.typelevel.ci.CIString

import scala.concurrent.ExecutionContext

class LiveLabelsSpec
    extends AsyncWordSpecLike
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with WiremockTestBase
    with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.global
  implicit val runtime: IORuntime                          = IORuntime.global

  val client: Client[IO]     = JavaNetClientBuilder[IO].create
  val labels: LiveLabels[IO] = new LiveLabels[IO](client, uri"http://localhost:8080")

  val source: Repository = Repository("test", "source")
  val rootUrl: String    = s"/repos/${source.owner}/${source.name}/labels"
  val label: Label       = Label(CIString("bug"), "f29513", Some("Something isn't working"))

  "getAll" should {
    val getAllBody =
      """[
        |  {
        |    "id": 208045946,
        |    "node_id": "MDU6TGFiZWwyMDgwNDU5NDY=",
        |    "url": "https://api.github.com/repos/octocat/Hello-World/labels/bug",
        |    "name": "bug",
        |    "description": "Something isn't working",
        |    "color": "f29513",
        |    "default": true
        |  },
        |  {
        |    "id": 208045947,
        |    "node_id": "MDU6TGFiZWwyMDgwNDU5NDc=",
        |    "url": "https://api.github.com/repos/octocat/Hello-World/labels/enhancement",
        |    "name": "enhancement",
        |    "description": "New feature or request",
        |    "color": "a2eeef",
        |    "default": false
        |  }
        |]
        |""".stripMargin

    "return list of labels" in {
      stubGetEndpoint(200, getAllBody, rootUrl)

      labels
        .getAll(source)
        .map {
          _ should contain theSameElementsAs List(
            Label(CIString("bug"), "f29513", Some("Something isn't working")),
            Label(CIString("enhancement"), "a2eeef", Some("New feature or request"))
          )
        }
        .unsafeToFuture()
    }

    "raise exception if unsuccessful request" in {
      stubGetEndpoint(404, getAllBody, rootUrl)

      recoverToExceptionIf[AppError] {
        labels.getAll(source).unsafeToFuture()
      }.map(_.msg should include regex s"get.*${source.fullname}")

    }
  }

  "create" should {

    val requestBody =
      s"""{
         |  "name": "${label.name}",
         |  "description": "${label.description.get}",
         |  "color": "${label.color}"
         |}
         |""".stripMargin

    "successfully create a label and return unit" in {

      val responseBody =
        s"""{
            |  "id": 208045946,
            |  "node_id": "MDU6TGFiZWwyMDgwNDU5NDY=",
            |  "url": "https://api.github.com/repos/octocat/Hello-World/labels/bug",
            |  "name": "${label.name}",
            |  "description": "${label.description.get}",
            |  "color": "${label.color}",
            |  "default": true
            |}
            |""".stripMargin
      stubPostEndpoint(201, requestBody, responseBody, rootUrl)

      labels.create(source, label).map(_ shouldBe ()).unsafeToFuture()
    }

    "response body does not matter for a successful request" in {
      stubPostEndpoint(201, requestBody, "", rootUrl)

      labels.create(source, label).map(_ shouldBe ()).unsafeToFuture()
    }

    "raise exception if unsuccessful request" in {
      stubPostEndpoint(500, requestBody, "", rootUrl)

      recoverToExceptionIf[AppError] {
        labels.create(source, label).unsafeToFuture()
      }.map(_.msg should include regex s"create.*${source.fullname}")
    }

  }

  // TODO: JavaNetClientBuilder does not support PATCH requests?
  "update" should {
    pending

    val requestBody =
      s"""{
         |  "name": "${label.name}",
         |  "description": "${label.description.get}",
         |  "color": "${label.color}"
         |}
         |""".stripMargin

    "successfully update a label and return unit" in {
      val responseBody =
        s"""{
           |  "id": 208045946,
           |  "node_id": "MDU6TGFiZWwyMDgwNDU5NDY=",
           |  "url": "https://api.github.com/repos/octocat/Hello-World/labels/bug",
           |  "name": "${label.name}",
           |  "description": "${label.description.get}",
           |  "color": "${label.color}",
           |  "default": true
           |}
           |""".stripMargin
      stubPatchEndpoint(200, requestBody, responseBody, s"$rootUrl/${label.name}")

      labels.update(source, label).map(_ shouldBe ()).unsafeToFuture()
    }

    "response body does not matter for a successful request" in {
      stubPatchEndpoint(200, requestBody, "", s"$rootUrl/${label.name}")

      labels.update(source, label).map(_ shouldBe ()).unsafeToFuture()
    }

    "raise exception if unsuccessful request" in {
      stubPatchEndpoint(500, requestBody, "", s"$rootUrl/${label.name}")

      recoverToExceptionIf[AppError] {
        labels.update(source, label).unsafeToFuture()
      }.map(_.msg should include regex s"update.*${source.fullname}")
    }

  }

  "delete" should {

    "successfully delete a label and return unit" in {
      stubDeleteEndpoint(204, s"$rootUrl/${label.name}")

      labels.delete(source, label.name.toString).map(_ shouldBe ()).unsafeToFuture()
    }

    "raise exception if unsuccessful request" in {
      stubDeleteEndpoint(500, s"$rootUrl/${label.name}")

      recoverToExceptionIf[AppError] {
        labels.delete(source, label.name.toString).unsafeToFuture()
      }.map(_.msg should include regex s"delete.*${source.fullname}")
    }

  }

}
