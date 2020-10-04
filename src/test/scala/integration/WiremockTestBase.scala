package integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

trait WiremockTestBase { self: BeforeAndAfterEach with BeforeAndAfterAll =>

  private val stubHost: String = "localhost"
  private val stubPort: Int    = 8080

  private val wiremockConfig = wireMockConfig()
    .port(stubPort)
    .bindAddress(stubHost)
    .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.NEVER)

  private val wiremockServer = new WireMockServer(wiremockConfig)

  override def beforeAll(): Unit = {
    wiremockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  override def afterEach(): Unit =
    wiremockServer.resetMappings()

  override def afterAll(): Unit =
    wiremockServer.stop()

  def stubGetEndpoint(status: Int, body: String, endpoint: String): StubMapping =
    stubFor(
      get(urlMatching(endpoint))
        .willReturn(
          aResponse
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody(body)
        )
    )

  def stubPostEndpoint(status: Int, requestBody: String, responseBody: String, endpoint: String): StubMapping =
    stubFor(
      post(urlMatching(endpoint))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        )
    )

  def stubPatchEndpoint(status: Int, requestBody: String, responseBody: String, endpoint: String): StubMapping =
    stubFor(
      patch(urlMatching(endpoint))
        .withRequestBody(equalToJson(requestBody, true, true))
        .willReturn(
          aResponse
            .withStatus(status)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)
        )
    )

  def stubDeleteEndpoint(status: Int, endpoint: String): StubMapping =
    stubFor(
      delete(urlMatching(endpoint))
        .willReturn(aResponse.withStatus(status))
    )

}
