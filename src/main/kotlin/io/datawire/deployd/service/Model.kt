package io.datawire.deployd.service

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.vertx.json.ObjectMappers

typealias Metadata = Map<String, String>


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = DockerImage::class, name = "docker")  // TODO: investigate how Jackson does type mapping as there is a tight coupling here.
)
abstract class Deployable

enum class PortProtocol {

    TCP, UDP;

    companion object {

        private val textAliases = mapOf("tcp" to TCP,
                                        "udp" to UDP)

        @JsonCreator
        @JvmStatic
        fun fromString(text: String) =
                textAliases[text.toLowerCase()] ?: throw IllegalArgumentException("Unknown protocol (provided: $text)")
    }
}

data class Networking(@JsonProperty val frontends: Collection<Frontend> = listOf(Frontend("default", FrontendType.HEADLESS, emptyList())),
                      @JsonProperty val backends: Collection<Backend>)

data class Backend(@JsonProperty val name: String,
                   @JsonProperty val port: Int,
                   @JsonProperty val protocol: PortProtocol = PortProtocol.TCP)

data class FrontendPort(@JsonProperty val port: Int,
                        @JsonProperty val backend: String)

enum class FrontendType {

    HEADLESS,
    INTERNAL,
    EXTERNAL,
    EXTERNAL_LOAD_BALANCER;

    companion object {

        private val textAliases = mapOf("none" to HEADLESS,
                                        "internal" to INTERNAL,
                                        "external" to EXTERNAL,
                                        "external:lb" to EXTERNAL_LOAD_BALANCER)

        @JsonCreator
        @JvmStatic
        fun fromString(text: String) =
                textAliases[text.toLowerCase()] ?: throw IllegalArgumentException("Unknown protocol (provided: $text)")
    }
}

data class Frontend(@JsonProperty val name: String,
                    @JsonProperty val type: FrontendType,
                    @JsonProperty val ports: Collection<FrontendPort>)

data class Descriptor(
        @JsonProperty val name: String,
        @JsonProperty val metadata: Metadata = emptyMap(),
        @JsonProperty val deployable: Deployable,
        @JsonProperty val networking: Networking
)

fun loadServiceDescriptor(text: String) = ObjectMappers.yamlMapper.readValue<Descriptor>(text)
