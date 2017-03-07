package io.datawire.deployd.service

import io.datawire.test.BaseTest
import org.junit.Test
import org.assertj.core.api.Assertions.*
import java.net.URI


class ModelTest : BaseTest() {

  val descriptor = """---
name: test

metadata:
  foo: bar
  baz: bot

deploy:
  type: docker
  registry: docker.io
  image: foo/bar
  resolver:
    type: provided

network:
  frontends:
    - name: test1
      type: external
      ports:
        - port: 5000
          backend: be-tcp
        - port: 5001
          backend: be-udp

  backends:
    - name: be-tcp
      port: 5000
    - name: be-udp
      protocol: udp
      port: 5001
"""

    @Test
    fun bindYamlServiceDescriptor() {
        val service = loadServiceDescriptor(descriptor)

        assertThat(service.name).isEqualTo("test")
        assertThat(service.metadata).isEqualTo(mapOf("foo" to "bar", "baz" to "bot"))
        assertThat(service.network.frontends).containsOnly(
                Frontend(name  = "test1",
                         type  = FrontendType.EXTERNAL,
                         ports = listOf(FrontendPort(5000, "be-tcp"), FrontendPort(5001, "be-udp"))))

        assertThat(service.network.backends).containsOnly(
                Backend("be-tcp", 5000, PortProtocol.TCP),
                Backend("be-udp", 5001, PortProtocol.UDP))

        assertThat(service.deploy).isInstanceOf(DockerImage::class.java)
        val deployable = service.deploy as DockerImage

        assertThat(deployable.registry).isEqualTo(URI.create("docker.io"))
        assertThat(deployable.image).isEqualTo("foo/bar")
        assertThat(deployable.resolver).isEqualTo(ProvidedDockerTagResolver())
    }
}