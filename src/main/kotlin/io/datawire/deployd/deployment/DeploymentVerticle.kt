package io.datawire.deployd.deployment

import io.datawire.deployd.kubernetes.KubernetesManager
import io.datawire.deployd.persistence.Workspace
import io.datawire.deployd.service.ServiceSpec
import io.datawire.deployd.terraform.RemoteParameters
import io.datawire.deployd.terraform.terraformSetup
import io.datawire.deployd.world.loadWorld
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.VerticleDeployer
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.DeploymentOptions
import java.nio.file.Paths


class DeploymentVerticle : BaseVerticle<DeploymentConfig>(DeploymentConfig::class) {

//    override fun start(startFuture: Future<Void>?) {
//        logger.info("ServiceSpec Manager starting")
//        registerEventBusCodec(DeploymentContext::class)
//        registerEventBusCodec(ServiceSpec::class)
//
//        checkKubernetesConnectivity()
//
//        startFuture?.complete()
//        start()
//    }
//
//    override fun stop(stopFuture: Future<Void>?) {
//        logger.info("ServiceSpec Manager stopping")
//        super.stop(stopFuture)
//    }
//
//    override fun start() {
//        vertx.eventBus().localConsumer<ServiceSpec>("deploy.ServiceSetup", this::handleService)
//        vertx.eventBus().localConsumer<DeploymentRequest>("deploy.DeploymentRequest", this::handleDeployment)
//        logger.info("ServiceSpec Manager started")
//    }
//
//    override fun stop() {
//        super.stop()
//        logger.info("ServiceSpec Manager stopped")
//    }
//
//    private fun checkKubernetesConnectivity() {
//        val ns = DefaultKubernetesClient().namespaces().withName("kube-system").get()
//        logger.debug("Found 'kube-system' namespace... We can talk to Kubernetes!")
//        ns ?: throw RuntimeException("Could not find main kubernetes namespace")
//    }
//
//    private fun handleService(msg: Message<ServiceSpec>) {
//        val world = loadWorld(vertx)
//        val servicePath = Workspace.path(vertx)
//        terraformSetup(
//                Paths.get(servicePath).resolve("services/${msg.body().name}"),
//                msg.body().name,
//                RemoteParameters(world.amazon))
//
//        val km  = KubernetesManager(DefaultKubernetesClient())
//        val ctx = DeploymentContext(msg.body(), null)
//        km.apply(ctx)
//    }
//
//    private fun handleDeployment(msg: Message<DeploymentRequest>) {
//        val request = msg.body()
//    }
//
//    companion object : VerticleDeployer() {
//        override fun deploy(vertx: Vertx, config: JsonObject, result: (AsyncResult<String>) -> Unit) {
//            val options = DeploymentOptions(
//                    config = config.getJsonObject("deployment"),
//                    worker = true)
//
//            vertx.deployVerticle(DeploymentVerticle::class.qualifiedName, options, result)
//        }
//    }
}