package io.datawire.deployd.deployment

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class DeploymentRequest(@JsonProperty val service: String,
                             @JsonProperty val dockerTag: String)

data class Deployment(@JsonProperty val id: UUID = UUID.randomUUID(),
                      @JsonProperty val service: String,
                      @JsonProperty val status: DeploymentStatus)

enum class DeploymentStatus {
    FAILED,
    IN_PROGRESS,
    NOT_STARTED,
    SUCCEEDED,
}