package io.datawire.deployd

import io.datawire.deployd.api.ApiVerticle
import io.datawire.deployd.world.AwsProvider
import io.datawire.deployd.world.World
import io.datawire.deployd.world.WorldRepository
import io.datawire.vertx.BaseVerticle
import io.vertx.core.Future


class Deployd : BaseVerticle<DeploydConfig>(DeploydConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        if (!vertx.fileSystem().existsBlocking(config().workspace.path.toString())) {
            vertx.fileSystem().mkdirBlocking(config().workspace.path.toString())
        }

        ApiVerticle.Deployer.deployRequired(vertx, originalConfig())

        startFuture?.complete()
        start()
    }

    override fun stop(stopFuture: Future<Void>?) {
        WorldRepository
                .load(vertx, ".deployd/worlds.json")
                .save(vertx, ".deployd/worlds.json")

        stopFuture?.complete()
        stop()
    }
}