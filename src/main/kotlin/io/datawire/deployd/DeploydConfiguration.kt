package io.datawire.deployd

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.api.Workspace
import io.dropwizard.Configuration
import java.nio.file.Path


data class DeploydConfiguration(@JsonProperty val workspace: Workspace,
                                @JsonProperty val terraform: TerraformConfig) : Configuration()

data class TerraformConfig(@JsonProperty val executable: Path)
