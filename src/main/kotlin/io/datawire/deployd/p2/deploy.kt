package io.datawire.deployd.p2


class KubernetesDeploy(private val provider: KubernetesProvider) {

  fun deploy(request: DeploymentRequest) {
    val kube = newClient(provider)


  }

  private fun rollingDeploy() {

  }
}

class TerraformDeploy {


}