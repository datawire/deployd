package io.datawire.tf

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.vertx.json.ObjectMappers
import java.io.File


data class TfModDescriptor(
        @JsonProperty val name    : String,
        @JsonProperty val version : Int,
        @JsonProperty val inputs  : Map<String, TfInput>,
        @JsonProperty val outputs : Map<String, String>)

data class TfInput(
        @JsonProperty val type: String,
        @JsonProperty val source: String,
        @JsonProperty val default: Any?)

fun loadDescriptor(path: String): TfModDescriptor {
    val mapper = ObjectMappers.yamlMapper
    return mapper.readValue(File(path))
}

fun main(args: Array<String>) {
    val module     = "/home/plombardi/datawire/blackbird/deployd-terraform/postgresql96/1"
    val descriptor = loadDescriptor(module + "/deployd-terraform.yaml")
    println(descriptor)

    val parameters = mapOf<String, String>(

    )
}