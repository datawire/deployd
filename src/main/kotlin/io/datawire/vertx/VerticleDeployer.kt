package io.datawire.vertx


import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.*
import io.vertx.core.logging.LoggerFactory


abstract class VerticleDeployer {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Deploy a verticle.
     *
     * @param vertx a Vertx instance to deploy the verticle.
     * @param config the root (top-level) JSON configuration object.
     * @param result callback for handling success or failure of the deployment.
     */
    abstract fun deploy(vertx: Vertx, config: JsonObject, result: (AsyncResult<String>) -> Unit)

    /**
     * Deploy a verticle unconditionally. If the verticle fails to deploy for whatever reason then the vertx instance
     * will be shutdown.
     *
     * @param vertx a Vertx instance to deploy the verticle.
     * @param config the root (top-level) JSON configuration object.
     */
    fun deployRequired(vertx: Vertx, config: JsonObject) {
        deploy(vertx, config, {
            if (it.failed()) {
                vertx.close()
                logger.error("Required verticle deploy failed. Server shutdown initiated!", it.cause())
            }
        })
    }
}