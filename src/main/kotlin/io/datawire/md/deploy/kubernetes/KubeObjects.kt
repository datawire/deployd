package io.datawire.md.deploy.kubernetes

import io.datawire.deployd.service.Backend
import io.datawire.deployd.service.Frontend
import io.datawire.deployd.service.FrontendType
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.extensions.*
import io.fabric8.kubernetes.api.model.extensions.Deployment as KubeDeployment


object KubeObjects {

    data class ServiceRef(val namespace: String, val name: String)

    data class DeploymentParameters(val service: ServiceRef,
                                    val version: String,
                                    val image: String,
                                    val rolling: Boolean,
                                    val environmentVariables: Map<String, String>,
                                    val replicas: Int = 1)

    data class ServiceParameters(val service: ServiceRef,
                                 val version: String?,
                                 val frontend: Frontend, val backends: Map<String, Backend>)

    fun newService(params: ServiceParameters): Service {
        val serviceSpec = ServiceSpecBuilder().apply {
            withSelector(mapOf("name" to params.service.name))
            withPorts(params.frontend.ports.map { (port, backendTarget) ->
                val backend = params.backends[backendTarget]!!
                ServicePort(
                        "fe-${params.frontend.name}-${backend.name}".toLowerCase(),
                        null,
                        port,
                        backend.protocol.toString(),
                        IntOrString(backend.name))
            })
        }

        when(params.frontend.type) {
            FrontendType.INTERNAL               -> serviceSpec.withType("ClusterIP")
            FrontendType.EXTERNAL               -> serviceSpec.withType("NodePort")
            FrontendType.EXTERNAL_LOAD_BALANCER -> serviceSpec.withType("LoadBalancer")
            FrontendType.HEADLESS               -> { serviceSpec.withType("None"); serviceSpec.withClusterIP("None") }
        }

        return ServiceBuilder().apply {
            withMetadata(metadata(
                    namespace = params.service.namespace,
                    name      = params.service.name,
                    labels    = mapOf("name" to params.service.name)
            ))

            withSpec(serviceSpec.build())
        }.build()
    }

    fun newDeployment(params: DeploymentParameters): KubeDeployment {
        return DeploymentBuilder().apply {
            withMetadata(metadata(
                    namespace = params.service.namespace,
                    name      = params.service.name,
                    labels    = mapOf(
                            "name" to params.service.name,
                            "version" to params.version
                    )
            ))
            withSpec(newDeploymentSpec(params))
        }.build()
    }

    fun newNamespace(name: String): Namespace = NamespaceBuilder().apply {
        withMetadata(metadata(
                name = name,
                labels = mapOf("name" to name))
        )
    }.build()

    private fun metadata(name: String?,
                         namespace: String? = null,
                         labels: Map<String, String> = emptyMap()) = ObjectMetaBuilder().apply {
        namespace?.let { withNamespace(namespace) }
        name?.let      { withName(name) }
        withLabels(labels)
    }.build()

    private fun newDeploymentSpec(params: DeploymentParameters): DeploymentSpec {
        return DeploymentSpecBuilder().apply {
            withPaused(false)
            withReplicas(params.replicas)

            if (!params.rolling) {
                withStrategy(DeploymentStrategy(null, "Recreate"))
            }

            withTemplate(newPodTemplate(params))
        }.build()
    }

    private fun newPodTemplate(params: DeploymentParameters): PodTemplateSpec {
        val podSpec = PodSpecBuilder().withContainers(newContainer(params)).build()
        return PodTemplateSpecBuilder().withSpec(podSpec).build()
    }

    private fun newContainer(params: DeploymentParameters): Container {
        return ContainerBuilder().apply {
            withImage(params.image)
            withImagePullPolicy("IfNotPresent") // TODO: probably needs to be configurable
        }.build()
    }

}