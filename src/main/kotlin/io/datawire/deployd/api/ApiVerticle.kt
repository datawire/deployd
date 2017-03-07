package io.datawire.deployd.api

import io.datawire.deployd.ApiConfig
import io.datawire.deployd.deployment.DeploymentsApi
import io.datawire.deployd.deployment.WorldsApi
import io.datawire.deployd.service.ServicesApi
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.VerticleDeployer
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.DeploymentOptions
import io.vertx.kotlin.core.json.get


class ApiVerticle : BaseVerticle<ApiConfig>(ApiConfig::class), Api {

    override fun start(startFuture: Future<Void>?) {
        logger.info("API starting (address: {}:{})", config().server.host, config().server.port)
        super.start(startFuture)
    }

    override fun stop(stopFuture: Future<Void>?) {
        logger.info("API stopping (address: {}:{})", config().server.host, config().server.port)
        super.stop(stopFuture)
    }

    override fun start() {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.route().handler({
            println("""${it.request().method()} ${it.request().path()}
${it.request().headers().toList()}


""")
            it.next()
        })

        // We're not serving a Favicon so we will not respond with one. Issues a 403 Forbidden.
        router.get("/favicon.ico").handler {
            it.response().setStatusCode(403).end()
        }

        with(router.get("/health")) {
            handler {
                it.response().setStatusCode(200).end()
            }
        }

        configure(router)

        vertx.createHttpServer().apply {
            requestHandler(router::accept)
            listen(config().server.port, config().server.host)
        }

        logger.info("API started (address: {}:{})", config().server.host, config().server.port)
    }

    override fun stop() {
        super.stop()
        logger.info("API stopped (address: {}:{})", config().server.host, config().server.port)
    }

    override fun configure(router: Router) {
        WorldsApi.configure(router)
        DeploymentsApi().configure(router)
        ServicesApi.configure(router)
    }

    object Deployer : VerticleDeployer() {
        override fun deploy(vertx: Vertx, config: JsonObject, result: (AsyncResult<String>) -> Unit) {
            val deployOptions = DeploymentOptions(config = config["api"])
            vertx.deployVerticle(ApiVerticle::class.qualifiedName, deployOptions, result)
        }
    }
}