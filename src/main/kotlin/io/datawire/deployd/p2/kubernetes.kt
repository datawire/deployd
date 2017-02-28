package io.datawire.deployd.p2

import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient


fun newClient(provider: KubernetesProvider): KubernetesClient = DefaultKubernetesClient()

fun newNamespace(name: String): Namespace {
  return NamespaceBuilder()
      .withMetadata(ObjectMetaBuilder().withName(name).build())
      .build()
}

fun newService(type: EntryPointType): Service {
  val builder = ServiceBuilder().apply {
    withApiVersion("v1")
    withSpec(ServiceSpecBuilder().apply {
      withType(toServiceType(type))
      withPorts()
    }.build())
  }

  return builder.build()
}

private fun toServiceType(type: EntryPointType): String {
  return when (type) {
    EntryPointType.PRIVATE       -> "ClusterIP"
    EntryPointType.PUBLIC        -> "NodePort"
    EntryPointType.LOAD_BALANCED -> "LoadBalancer"
  }
}