package io.datawire.deployd

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.api.ApiVerticle
import io.datawire.deployd.deployment.DeploymentVerticle
import io.datawire.deployd.persistence.Workspace
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.Future
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject


data class Test(@JsonProperty val foo: String)


class MicroDeploy : BaseVerticle<DeploydConfig>(DeploydConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        bootstrap()

        Workspace.initialize(vertx, config().workspace)

        ApiVerticle.deployRequired(vertx, originalConfig())
        DeploymentVerticle.deployRequired(vertx, originalConfig())

        startFuture?.complete()
        start()
    }

    override fun stop(stopFuture: Future<Void>?) {

        LocalMapWorldRepo.get(vertx).save(vertx.fileSystem())

        stopFuture?.complete()
        stop()
    }

    private fun bootstrap() {
        ObjectMappers.configure(Json.mapper)
        ObjectMappers.configure(Json.prettyMapper)
    }
}