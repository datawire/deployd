package io.datawire.deployd.service

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.datawire.deployd.deployment.DeploymentRequest
import java.net.URI


data class DockerImage(@JsonProperty val registry: URI,
                       @JsonProperty val image: String,
                       @JsonProperty val resolver: DockerTagResolver) : Deployable()

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = ProvidedDockerTagResolver::class, name = "provided"),
        JsonSubTypes.Type(value = QueryDockerTagResolver::class,    name = "queried")
)
interface DockerTagResolver {
    fun resolve(request: DeploymentRequest): String
}

data class ProvidedDockerTagResolver(val workaround: String? = null) : DockerTagResolver {
    override fun resolve(request: DeploymentRequest) = request.dockerTag
}

data class QueryDockerTagResolver(@JsonProperty val address: URI) : DockerTagResolver {
    override fun resolve(request: DeploymentRequest): String {
        throw NotImplementedError()
    }
}