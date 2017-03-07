package io.datawire.deployd

import io.datawire.deployd.api.ApiVerticle
import io.datawire.deployd.service.Service
import io.datawire.deployd.world.World
import io.datawire.vertx.BaseVerticle
import io.vertx.core.Future


class MicroDeploy : BaseVerticle<DeploydConfig>(DeploydConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        if (!vertx.fileSystem().existsBlocking(config().workspace.path.toString())) {
            vertx.fileSystem().mkdirBlocking(config().workspace.path.toString())
        }

        ApiVerticle.Deployer.deployRequired(vertx, originalConfig())

        startFuture?.complete()
        start()
    }

    override fun stop(stopFuture: Future<Void>?) {

        WorldRepo.getInstance<World>(vertx, "worlds").store(vertx, ".deployd/worlds.json")
        ServiceRepo.getInstance<Service>(vertx, "service").store(vertx, ".deployd/services.json")

        stopFuture?.complete()
        stop()
    }
}