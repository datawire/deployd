package io.datawire.deployd.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
    fun resolve(): String
}

data class ProvidedDockerTagResolver(@JsonProperty val tag: String) : DockerTagResolver {
    override fun resolve() = tag
}

data class QueryDockerTagResolver(@JsonProperty val address: URI) : DockerTagResolver {
    override fun resolve(): String {
        throw NotImplementedError()
    }
}