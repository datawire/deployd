package io.datawire.deployd.deployment

import io.datawire.deployd.api.Api
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext


class DeploymentApi : Api {

    override fun configure(router: Router) {
        with(router.post("/deployments")) {
            consumes("application/json")
            produces("application/json")
            handler(this@DeploymentApi::addDeployment)
        }

        with(router.post("/deployments")) {
            produces("application/json")
            handler(this@DeploymentApi::getDeployments)
        }

        with(router.get("/deployments/:id")) {
            produces("application/json")
            handler(this@DeploymentApi::getDeployment)
        }

        with(router.delete("/deployments/:id")) {
            produces("application/json")
            handler(this@DeploymentApi::cancelDeployment)
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