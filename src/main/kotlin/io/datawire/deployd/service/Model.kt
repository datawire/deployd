package io.datawire.deployd.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.shareddata.Shareable

typealias Metadata = Map<String, String>

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = TerraformRequirement::class, name = "terraform")  // TODO: investigate how Jackson does type mapping as there is a tight coupling here.
)
abstract class Requirement

data class TerraformRequirement(@JsonProperty val name: String,
                                @JsonProperty val module: String,
                                @JsonProperty val params: Map<String, *>) : Requirement()

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = DockerImage::class, name = "docker")  // TODO: investigate how Jackson does type mapping as there is a tight coupling here.
)
abstract class Deployable

enum class PortProtocol {
    @JsonProperty("tcp") TCP,
    @JsonProperty("udp") UDP
}

data class Network(@JsonProperty val frontends: Collection<Frontend> = listOf(Frontend("default", FrontendType.HEADLESS, emptyList())),
                   @JsonProperty val backends: Collection<Backend>) : Shareable

data class Backend(@JsonProperty val name: String,
                   @JsonProperty val port: Int,
                   @JsonProperty val protocol: PortProtocol = PortProtocol.TCP) : Shareable

data class FrontendPort(@JsonProperty val port: Int,
                        @JsonProperty val backend: String) : Shareable

enum class FrontendType {
    @JsonProperty("none")
    HEADLESS,

    @JsonProperty("internal")
    INTERNAL,

    @JsonProperty("external")
    EXTERNAL,

    @JsonProperty("external:load-balancer")
    EXTERNAL_LOAD_BALANCER
}

data class Frontend(@JsonProperty val name: String,
                    @JsonProperty val type: FrontendType,
                    @JsonProperty val ports: Collection<FrontendPort>) : Shareable

data class Service(@JsonProperty val name: String,
                   @JsonProperty val metadata: Metadata = emptyMap(),
                   @JsonProperty val deploy: Deployable,
                   @JsonProperty val network: Network,
                   @JsonProperty val requires: List<Requirement> = emptyList()) : Shareable

fun loadServiceDescriptor(text: String) = ObjectMappers.yamlMapper.readValue<Service>(text)

fun checkFrontendToBackendPortMapping(frontend: Frontend, backends: Collection<Backend>) {
    val backendMap = backends.associateBy { it.name }
    for ((port, backend) in frontend.ports) {
        if (backend !in backendMap) {
            throw IllegalStateException(
                    "Frontend['${frontend.name}'].port[$port] is mapped to an unknown or missing backend['$backend']")
        }
    }
}