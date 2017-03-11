package io.datawire.md.fabric

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped


data class Parameters(private val map: Map<String, Any?>): Map<String, Any?> by map


data class PlanningContext(val deployId     : String,
                           val moduleSpec   : TfModuleSpec,
                           val parameters   : Parameters)


data class TfModuleSpec(@JsonProperty val name    : String,
                        @JsonProperty val version : Int,
                        @JsonProperty val source  : String,
                        @JsonProperty val inputs  : Map<String, TfVariableSpec>,
                        @JsonProperty val outputs : Map<String, String>) {

    val id get() = "${name.toLowerCase()}-v$version"
}

data class TfVariableSpec(
        @JsonProperty val type: String,
        @JsonProperty val source: String)


data class TfVariables(@get:JsonAnyGetter val map: Map<String, Any?> = emptyMap())


data class TfModule(@JsonProperty val source: String,

                    @JsonProperty
                    @JsonUnwrapped
                    val variables: TfVariables = TfVariables()) {}

class TfTemplate(@JsonProperty("module") val modules: Map<String, TfModule>)