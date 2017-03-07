package io.datawire.deployd.deployment

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.deployd.api.Api
import io.datawire.deployd.world.World
import io.datawire.deployd.world.WorldRepository
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext


object WorldsApi : Api {

    override fun configure(router: Router) {
        with(router.post("/worlds")) {
            consumes("application/json")
            produces("application/json")
            handler(this@WorldsApi::addWorld)
        }

        with(router.get("/worlds/:name")) {
            produces("application/json")
            handler(this@WorldsApi::getWorld)
        }

        with(router.get("/worlds")) {
            produces("application/json")
            handler(this@WorldsApi::getWorlds)
        }
    }

    private fun addWorld(ctx: RoutingContext) {
        val resp = ctx.response()

        val world = ObjectMappers.mapper.readValue<World>(ctx.bodyAsString)
        val worldRepo = WorldRepository.load(ctx.vertx(), ".deployd/worlds.json")

        worldRepo.addWorld(world)

        resp.apply {
            statusCode = 204
            end()
        }
    }

    private fun getWorld(ctx: RoutingContext) {
        val resp = ctx.response()

        val worldRepo = WorldRepository.load(ctx.vertx(), ".deployd/worlds.json")
        val world = worldRepo.getWorld(ctx.pathParam("name"))

        if (world != null) {
            resp.setStatusCode(200)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(result(world.copy(amazon = world.amazon.copy(secretKey = null))))
        } else {
            resp.setStatusCode(404).end()
        }
    }

    private fun getWorlds(ctx: RoutingContext) {
        val resp = ctx.response()

        val worldRepo = WorldRepository.load(ctx.vertx(), ".deployd/worlds.json")
        val worlds = worldRepo.getWorlds().map {
            it.copy(amazon = it.amazon.copy(secretKey = null))
        }

        resp.apply {
            statusCode = 200
            putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            end(collectionResult("worlds", worlds))
        }
    }
}


class DeploymentsApi : Api {

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
        ctx.next()
    }

    private fun cancelDeployment(ctx: RoutingContext) {
        ctx.next()
    }
}