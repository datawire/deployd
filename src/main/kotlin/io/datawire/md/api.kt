package io.datawire.md

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.deployd.ApiConfig
import io.datawire.deployd.service.ServiceSpec
import io.datawire.md.deploy.Deployment
import io.datawire.md.fabric.FabricApi
import io.datawire.md.service.ServiceApi
import io.datawire.md.service.validate
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.VerticleDeployer
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.DeploymentOptions
import io.vertx.kotlin.core.json.get


private const val JSON_TYPE = "application/json"
private const val YAML_TYPE = "application/yaml"


class Api : BaseVerticle<ApiConfig>(ApiConfig::class) {

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
//        router.route().handler(BodyHandler.create())
//        router.route().handler({
//            println("""${it.request().method()} ${it.request().path()}
//${it.request().headers().toList()}
//
//
//""")
//            it.next()
//        })

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

    companion object : VerticleDeployer() {
        override fun deploy(vertx: Vertx, config: JsonObject, result: (AsyncResult<String>) -> Unit) {
            val deployOptions = DeploymentOptions(config = config["api"])
            vertx.deployVerticle(Api::class.qualifiedName, deployOptions, result)
        }
    }
}

inline fun <reified T: Any> readBody(contentType: String, buffer: Buffer): T {
    val ct = contentType.toLowerCase()
    return when {
        ct.endsWith("json") -> ObjectMappers.mapper.readValue(buffer.toString())
        ct.endsWith("yaml") -> ObjectMappers.yamlMapper.readValue(buffer.toString())
        else                -> throw IllegalArgumentException("Cannot handle content type: '$contentType'")
    }
}

private fun configure(router: Router) {
    router.post("/fabric").apply {
        consumes(JSON_TYPE)
        consumes(YAML_TYPE)
        produces(JSON_TYPE)
        handler(FabricApi::putFabric)
    }

    router.get("/fabric").apply             { produces(JSON_TYPE); handler(FabricApi::getFabric) }
    router.get("/fabric/modules").apply     { produces(JSON_TYPE); handler(FabricApi::listModules) }
    router.get("/fabric/modules/:id").apply { produces(JSON_TYPE); handler(FabricApi::getModule) }
    router.post("/fabric/modules").apply {
        consumes(JSON_TYPE)
        consumes(YAML_TYPE)
        handler(FabricApi::putModule)
    }

    router.post("/services").apply {
        consumes(JSON_TYPE)
        consumes(YAML_TYPE)
        produces(JSON_TYPE)
        handler(ServiceApi::upsertService)
    }

    router.get("/services").apply       { produces(JSON_TYPE); handler(ServiceApi::listServices) }
    router.get("/services/:name").apply { produces(JSON_TYPE); handler(ServiceApi::getService) }

    router.post("/services/:name/deploy").apply {
        consumes(JSON_TYPE)
        produces(JSON_TYPE)
    }
}

private fun upsertService(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val req = ctx.request()

    req.bodyHandler { body ->
        val service = readBody<ServiceSpec>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)

        validate(vertx, service)
        vertx.eventBus().send<Deployment>("deploy.service:upsert", service) { reply ->
            if (reply.succeeded()) {
                jsonResponse(ctx, reply.result().body())
            } else {
                ctx.fail(reply.cause())
            }
        }
    }
}

private fun deployService(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val req = ctx.request()

    req.bodyHandler { body ->
        val service = readBody<ServiceSpec>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)

        validate(vertx, service)
        vertx.eventBus().send<ServiceSpec>("deploy.service:deploy", service) {

        }
    }
}































