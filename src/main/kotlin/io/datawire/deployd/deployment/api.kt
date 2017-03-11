package io.datawire.deployd.deployment

import io.datawire.deployd.api.fromJson
import io.datawire.deployd.service.FileSystemServiceRepo
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext


fun configure(router: Router) {
    with(router.post("/deployments")) {
        consumes("application/json")
        produces("application/json")
        handler(::addDeployment)
    }

    with(router.post("/deployments")) {
        produces("application/json")
        handler(::getDeployments)
    }

    with(router.get("/deployments/:id")) {
        produces("application/json")
        handler(::getDeployment)
    }

    with(router.delete("/deployments/:id")) {
        produces("application/json")
        handler(::cancelDeployment)
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