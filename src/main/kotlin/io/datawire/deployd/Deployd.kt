package io.datawire.deployd

import io.datawire.deployd.api.ApiVerticle
import io.datawire.vertx.BaseVerticle
import io.vertx.core.Future


class Deployd : BaseVerticle<DeploydConfig>(DeploydConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        ApiVerticle.Deployer.deployRequired(vertx, originalConfig())

        startFuture?.complete()
        start()
    }
}