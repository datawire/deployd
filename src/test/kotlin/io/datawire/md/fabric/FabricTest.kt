package io.datawire.md.fabric

import io.datawire.md.fromYaml
import io.datawire.test.BaseTest
import org.assertj.core.api.Assertions.*
import org.junit.Test


class FabricTest : BaseTest() {

    private val fabricYaml = """---
name: alpha
parameters:
  vpc_id: vpc-SunDial
  subnets:
    - subnet-AliceInChains
    - subnet-KillingJoke

amazon:
  access_key: LedZeppelin
  secret_key: Nirvana
  region: us-east-1

terraform:
  state_bucket: PinkFloyd
"""

    @Test
    fun mapSimpleFabricSpecFromYaml() {
        val fabricSpec = fromYaml<FabricSpec>(fabricYaml)
        assertThat(fabricSpec.name).isEqualTo("alpha")
        assertThat(fabricSpec.parameters)
                .isEqualTo(mapOf(
                        "vpc_id" to "vpc-SunDial",
                        "subnets" to listOf("subnet-AliceInChains", "subnet-KillingJoke")))

        assertThat(fabricSpec.amazon.accessKey).isEqualTo("LedZeppelin")
        assertThat(fabricSpec.amazon.secretKey).isEqualTo("Nirvana")
        assertThat(fabricSpec.amazon.region).isEqualTo("us-east-1")

        assertThat(fabricSpec.terraform.stateBucket).isEqualTo("PinkFloyd")
    }
}