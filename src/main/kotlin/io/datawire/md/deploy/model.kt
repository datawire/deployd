package io.datawire.md.deploy

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.service.ServiceSpec
import io.datawire.md.service.ServiceRef


enum class DeploymentStatus {
    @JsonProperty("not-started")
    NOT_STARTED,

    @JsonProperty("in-progress")
    IN_PROGRESS,

    @JsonProperty("succeeded")
    SUCCEEDED,

    @JsonProperty("failed")
    FAILED
}

enum class UpdateType {
    @JsonProperty("specification")
    SPECIFICATION_UPDATE,

    @JsonProperty("implementation")
    IMPLEMENTATION_UPDATE
}


data class Deployment(
        @JsonProperty val id      : String,
        @JsonProperty val update  : UpdateType,
        @JsonProperty val status  : DeploymentStatus,
        @JsonProperty val service : ServiceRef)


data class DeploymentContext(
        @JsonProperty val deployment: Deployment,
        @JsonProperty val service: ServiceSpec)


data class DeploymentRef(val service: ServiceRef, val id: String)