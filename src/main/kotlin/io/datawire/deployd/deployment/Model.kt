package io.datawire.deployd.deployment

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.service.Service
import java.util.*

data class DeploymentRequest(@JsonProperty val service: String,
                             @JsonProperty val dockerTag: String)

data class DeploymentContext(@JsonProperty val service: Service, val request: DeploymentRequest?)

data class Deployment(@JsonProperty val id: UUID = UUID.randomUUID(),
                      @JsonProperty val service: String,
                      @JsonProperty val status: DeploymentStatus)

enum class DeploymentStatus {
    FAILED,
    IN_PROGRESS,
    NOT_STARTED,
    SUCCEEDED,
}