package io.datawire.deployd.service

import io.datawire.deployd.ApiConfig
import io.datawire.deployd.ServerConfig
import io.datawire.deployd.api.ApiVerticle
import io.datawire.test.BaseTestUsingVertx
import io.vertx.core.DeploymentOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestContext
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.net.URI


class ApiTest : BaseTestUsingVertx() {

    @get:Rule val name = TestName()

    private val baseAPIPath = "/services"
    private var serverPort = -1

    override fun setup(ctx: TestContext) {
        serverPort = reserveListenPort()

        val apiConfig = ApiConfig(
                server = ServerConfig(port = serverPort)
        )

        val apiConfigAsJson = JsonObject(objectMapper.writeValueAsString(apiConfig))

        val options = DeploymentOptions().setConfig(apiConfigAsJson)
        vertx.deployVerticle(ApiVerticle::class.qualifiedName, options, ctx.asyncAssertSuccess())
    }

    @Test
    fun registerService_ReturnsHttp200AndServiceJson(ctx: TestContext) {
        val async = ctx.async()

        val service = Service(
                name    = "foobar-service",
                deploy  = DockerImage(URI.create("foobar.io"), "foo/bar", ProvidedDockerTagResolver()),
                network = Network(
                        frontends = listOf(Frontend("public", FrontendType.HEADLESS, emptyList())),
                        backends  = listOf(Backend("api", 5001))
                )
        )

        val http = vertx.createHttpClient()
        val request = http.post(serverPort, "localhost", baseAPIPath)
        request.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        request.putHeader(HttpHeaders.ACCEPT, "application/json")

        request.handler { resp ->
            resp.exceptionHandler { ctx.fail(it) }

            ctx.assertEquals(200, resp.statusCode())
            ctx.assertEquals("application/json", resp.getHeader(HttpHeaders.CONTENT_TYPE))

            resp.bodyHandler { data ->
                val raw = data.toJsonObject()
                val returnedSvc = fromJson<Service>(raw)

                ctx.assertEquals(service, returnedSvc)

                async.complete()
            }
        }

        request.end(toJson(service))
    }
}