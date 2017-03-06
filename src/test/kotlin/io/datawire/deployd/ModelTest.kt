package io.datawire.deployd

import org.junit.Test
import org.assertj.core.api.Assertions.*


class ModelTest {

  val descriptor = """---
name: test

metadata:
  foo: bar
  baz: bot

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
    }
}