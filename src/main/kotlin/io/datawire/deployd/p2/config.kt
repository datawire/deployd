package io.datawire.deployd.p2

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import java.nio.file.Path


data class TerraformConfig(@JsonProperty val executable: Path)

data class DeploydConfig(@JsonProperty val terraform: TerraformConfig) : Configuration()
