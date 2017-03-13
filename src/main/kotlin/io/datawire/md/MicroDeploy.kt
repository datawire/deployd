package io.datawire.md

import io.datawire.md.deploy.DeploymentVerticle
import io.datawire.deployd.persistence.Workspace
import io.datawire.md.Api
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.Future
import io.vertx.core.json.Json


class MicroDeploy : BaseVerticle<DeploydConfig>(DeploydConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        bootstrap()

        Workspace.initialize(vertx, config().workspace)

        Api.deployRequired(vertx, originalConfig())
        DeploymentVerticle.deployRequired(vertx, originalConfig())

        startFuture?.complete()
        start()
    }

    override fun stop(stopFuture: Future<Void>?) {
        stopFuture?.complete()
        stop()
    }

    private fun bootstrap() {
        ObjectMappers.configure(Json.mapper)
        ObjectMappers.configure(Json.prettyMapper)
    }
}