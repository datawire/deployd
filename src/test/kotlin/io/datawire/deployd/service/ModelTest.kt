package io.datawire.deployd.service

import io.datawire.deployd.service.*
import org.junit.Test
import org.assertj.core.api.Assertions.*
import java.net.URI


class ModelTest {

  val descriptor = """---
name: test

metadata:
  foo: bar
  baz: bot

deployable:
  type: docker
  registry: docker.io
  image: foo/bar
  resolver:
    type: provided
    tag: 1.0

networking:
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
        assertThat(service.networking.frontends).containsOnly(
                Frontend(name  = "test1",
                         type  = FrontendType.EXTERNAL,
                         ports = listOf(FrontendPort(5000, "be-tcp"), FrontendPort(5001, "be-udp"))))

        assertThat(service.networking.backends).containsOnly(
                Backend("be-tcp", 5000, PortProtocol.TCP),
                Backend("be-udp", 5001, PortProtocol.UDP))

        assertThat(service.deployable).isInstanceOf(DockerImage::class.java)
        val deployable = service.deployable as DockerImage

        assertThat(deployable.registry).isEqualTo(URI.create("docker.io"))
        assertThat(deployable.image).isEqualTo("foo/bar")
        assertThat(deployable.resolver).isEqualTo(ProvidedDockerTagResolver("1.0"))
    }
}