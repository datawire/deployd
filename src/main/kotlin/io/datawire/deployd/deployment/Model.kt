package io.datawire.deployd.deployment

import com.fasterxml.jackson.annotation.JsonProperty

data class DeploymentRequest(@JsonProperty val service: String)

data class Deployment(@JsonProperty val id: String,
                      @JsonProperty val service: String,
                      @JsonProperty val status: DeploymentStatus)

enum class DeploymentStatus {
    FAILED,
    IN_PROGRESS,
    NOT_STARTED,
    SUCCEEDED,
}