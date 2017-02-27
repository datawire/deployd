package io.datawire.deployd.terraform

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.dropwizard.jackson.Jackson
import java.nio.file.Path


data class TerraformTemplate(
    @JsonProperty("variable") val variables: Map<String, Map<String, Any?>>,
    @JsonProperty("module")   val modules: Map<String, Map<String, Any?>>
) {

  fun renderToFile(path: Path) {
    val mapper = Jackson.newObjectMapper().registerKotlinModule()
    mapper.writeValue(path.toFile(), this)
  }
}