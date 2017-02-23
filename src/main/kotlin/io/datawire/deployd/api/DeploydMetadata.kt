package io.datawire.deployd.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Path


data class DeploydMetadata(@JsonProperty("@_deployd_version") val version: Int,
                           @JsonProperty val service: ServiceMetadata,
                           @JsonProperty val docker: DockerRepository,
                           @JsonProperty val worlds: List<String>,
                           @JsonProperty val requires: List<String>) {

    companion object {

        fun load(mapper: ObjectMapper, path: Path): DeploydMetadata {
            return try {
                mapper.readValue(path.toFile(), DeploydMetadata::class.java)
            } catch (ex: Exception) {
                ex.printStackTrace()
                throw ex
            }
        }
    }
}