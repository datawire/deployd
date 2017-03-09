package io.datawire.deployd.deployment

import io.datawire.deployd.api.Api
import io.datawire.deployd.api.fromJson
import io.datawire.deployd.service.FileSystemServiceRepo
import io.datawire.deployd.world.World
import io.datawire.deployd.world.loadWorld
import io.datawire.deployd.world.writeWorld
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext


object WorldsApi : Api {

    override fun configure(router: Router) {
        with(router.post("/world")) {
            consumes("application/json")
            produces("application/json")
            handler(this@WorldsApi::addWorld)
        }

        with(router.get("/world")) {
            produces("application/json")
            handler(this@WorldsApi::getWorld)
        }
    }

    private fun addWorld(ctx: RoutingContext) {
        val resp = ctx.response()

        val world = ctx.bodyAsJson.mapTo(World::class.java)
        writeWorld(ctx.vertx(), world)

        resp.apply {
            statusCode = 204
            end()
        }
    }

    private fun getWorld(ctx: RoutingContext) {
        val resp = ctx.response()

        val world = loadWorld(ctx.vertx())

        if (world != null) {
            resp.setStatusCode(200)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(result(world.copy(amazon = world.amazon.copy(secretKey = null))))
        } else {
            resp.setStatusCode(404).end()
        }
    }
}


class DeploymentsApi : Api {

    private val logger = LoggerFactory.getLogger(DeploymentsApi::class.java)

    override fun configure(router: Router) {
        with(router.post("/deployments")) {
            consumes("application/json")
            produces("application/json")
            handler(this@DeploymentsApi::addDeployment)
        }

        with(router.post("/deployments")) {
            produces("application/json")
            handler(this@DeploymentsApi::getDeployments)
        }

        with(router.get("/deployments/:id")) {
            produces("application/json")
            handler(this@DeploymentsApi::getDeployment)
        }

        with(router.delete("/deployments/:id")) {
            produces("application/json")
            handler(this@DeploymentsApi::cancelDeployment)
        }
    }

    private fun getDeployment(ctx: RoutingContext) {
        ctx.next()
    }

    private fun getDeployments(ctx: RoutingContext) {
        ctx.next()
    }

    private fun addDeployment(ctx: RoutingContext) {
        val deployRequest = fromJson<DeploymentRequest>(ctx.bodyAsString)
        val svcRepo = FileSystemServiceRepo(ctx.vertx())
        val service = svcRepo.getService(deployRequest.service)

        service?.let {

            val deployCtx = DeploymentContext(it, deployRequest)

            ctx.vertx().eventBus().send("deploy.DeploymentRequest", deployCtx)
            ctx.response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end()

        } ?: ctx.fail(404)
    }

    private fun cancelDeployment(ctx: RoutingContext) {
        ctx.next()
    }
}