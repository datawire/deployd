package io.datawire.deployd.p2

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.net.URI


data class DeploymentRequest(@JsonProperty val dockerTag: DockerTag)


data class DeploymentContext(@JsonProperty val name: String,
                             @JsonProperty val service: Service)


data class AwsProvider(@JsonProperty val accessKey: String?,
                       @JsonProperty val secretKey: String?,
                       @JsonProperty val region: String)


data class KubernetesProvider(@JsonProperty val address: URI)


interface Identifiable {
  val id: String
}


data class World(@JsonProperty val name: String,
                 @JsonProperty val amazon: AwsProvider?,
                 @JsonProperty val params: Parameters): Identifiable {

  override val id
    @JsonProperty get() = name
}


data class Service(@JsonProperty val name: String,
                   @JsonProperty val artifact: Artifact,
                   @JsonProperty val entryPoint: EntryPoint,
                   @JsonProperty val requirements: List<Dependency>): Identifiable {

  override val id
    @JsonProperty get() = name
}

enum class EntryPointType {

  PRIVATE,
  PUBLIC,
  LOAD_BALANCED;

  companion object {
    fun fromString(value: String): EntryPointType {
      return when(value.toLowerCase()) {
        "private"              -> PRIVATE
        "public"               -> PUBLIC
        "public:load-balanced" -> LOAD_BALANCED
        else -> throw IllegalArgumentException("Invalid EntryPointType <$value>")
      }
    }
  }
}

data class EntryPoint(@JsonProperty val port: Int,
                      @JsonProperty val targetPort: Int?,
                      @JsonProperty val type: EntryPointType)


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(name = "docker-image", value = DockerImage::class)
)
abstract class Artifact


data class DockerImage(@JsonProperty val name: String,
                       @JsonProperty val tag: String? = null,
                       @JsonProperty val registry: URI = URI.create("docker.io")) : Artifact()


typealias DockerTag = String


data class DependencyDefinition(@JsonProperty val name: String)


data class Dependency(@JsonProperty val definition: DependencyDefinition,
                      @JsonProperty val name: String)


data class Parameters(private val backingMap: Map<String, String>) {

  constructor(): this(mapOf())

  @JsonAnyGetter
  fun toMap(): Map<String, String> = LinkedHashMap(backingMap)
}

