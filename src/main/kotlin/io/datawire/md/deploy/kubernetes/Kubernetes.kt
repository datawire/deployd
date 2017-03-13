package io.datawire.md.deploy.kubernetes

import io.datawire.deployd.deployment.DeploymentContext
import io.datawire.deployd.service.Backend
import io.datawire.deployd.service.Frontend
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.client.KubernetesClient
import io.vertx.core.logging.LoggerFactory

sealed class KubernetesOperation(protected val kubernetes: KubernetesClient) {
    abstract fun apply()
}

class CreateNamespace(kubernetes: KubernetesClient,
                      private val name: String) : KubernetesOperation(kubernetes) {

    override fun apply() {
        val ns = KubeObjects.newNamespace(name)
        kubernetes.namespaces().create(ns)
    }
}

class CreateService(kubernetes: KubernetesClient,
                    private val name: String,
                    private val frontend: Frontend,
                    private val backends: Map<String, Backend>) : KubernetesOperation(kubernetes) {

    override fun apply() {
        val srvRef = KubeObjects.ServiceRef(name, name)

        val params  = KubeObjects.ServiceParameters(srvRef, null, frontend, backends)
        val service = KubeObjects.newService(params)
        kubernetes.services().create(service)
    }
}

class CreateDeployment(kubernetes: KubernetesClient) : KubernetesOperation(kubernetes) {

    override fun apply() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


class KubernetesManager(private val client: KubernetesClient) {

    private val logger = LoggerFactory.getLogger(KubernetesManager::class.java)

    fun apply(ctx: DeploymentContext) {
        // TODO: This can all likely be merged nicely in the future.
        if (findNamespace(ctx.service.name) == null) {
            create(ctx)
        } else {
            update(ctx)
        }
    }

    private fun findNamespace(name: String): Namespace? = client.namespaces().withName(name).get()

    private fun update(ctx: DeploymentContext) {

        // we shouldn't need to do this for anything but append-only and we're not doing that ATM.
//        val k8sService = CreateService(
//                client,
//                ctx.service.name,
//                ctx.service.network.frontends.first(),
//                ctx.service.network.backends.associateBy { it.name })

        val k8sDeploy = CreateDeployment(client)
        val operations = listOf(k8sDeploy)
        for (op in operations) {
            op.apply()
        }
    }

    private fun create(ctx: DeploymentContext) {
        val k8sNamespace = CreateNamespace(client, ctx.service.name)

        // TODO: kubernetes v1.ServiceSpec creation needs to be more advanced than this
        val k8sService = CreateService(
                client,
                ctx.service.name,
                ctx.service.network.frontends.first(),
                ctx.service.network.backends.associateBy { it.name })

        val operations = listOf(
                k8sNamespace,
                k8sService
        )

        for (op in operations) {
            op.apply()
        }
    }
}