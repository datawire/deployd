package io.datawire.deployd

import io.datawire.deployd.api.ApiVerticle
import io.datawire.deployd.deployment.DeploymentVerticle
import io.datawire.vertx.BaseVerticle
import io.vertx.core.Future


class MicroDeploy : BaseVerticle<DeploydConfig>(DeploydConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        if (!vertx.fileSystem().existsBlocking(config().workspace.path.toString())) {
            vertx.fileSystem().mkdirBlocking(config().workspace.path.toString())
        }

        ApiVerticle.deployRequired(vertx, originalConfig())
        DeploymentVerticle.deployRequired(vertx, originalConfig())

        startFuture?.complete()
        start()
    }

    override fun stop(stopFuture: Future<Void>?) {

        LocalMapWorldRepo.get(vertx).save(vertx.fileSystem())
        LocalMapServiceRepo.get(vertx).save(vertx.fileSystem())

        stopFuture?.complete()
        stop()
    }
}