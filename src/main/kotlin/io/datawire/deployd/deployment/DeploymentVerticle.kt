package io.datawire.deployd.deployment

import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.VerticleDeployer
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.DeploymentOptions


class DeploymentVerticle : BaseVerticle<DeploymentConfig>(DeploymentConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        registerEventBusCodec(DeploymentRequest::class)
    }

    override fun stop(stopFuture: Future<Void>?) {
        super.stop(stopFuture)
    }

    override fun start() {
        vertx.eventBus().localConsumer<DeploymentRequest>("deploy.DeploymentRequest", this::handleDeployment)


    }

    override fun stop() {
        super.stop()
    }

    private fun handleDeployment(msg: Message<DeploymentRequest>) {

    }

    companion object : VerticleDeployer() {
        override fun deploy(vertx: Vertx, config: JsonObject, result: (AsyncResult<String>) -> Unit) {
            val options = DeploymentOptions(
                    config = config.getJsonObject("deployment"),
                    worker = true)

            vertx.deployVerticle(DeploymentVerticle::class.qualifiedName, options, result)
        }
    }
}