package io.datawire.md.fabric

import com.fasterxml.jackson.annotation.*
import io.vertx.core.shareddata.Shareable


data class FabricSpec(@JsonProperty val name: String,
                      @JsonProperty val parameters: Map<String, *>,
                      @JsonProperty val amazon: AwsProvider,
                      @JsonProperty val terraform: TerraformProvider)


data class AwsProvider(@JsonProperty("access_key") val accessKey: String?,
                       @JsonProperty("secret_key") val secretKey: String?,
                       @JsonProperty("region")     val region: String) : Shareable

data class AwsNetwork(@JsonProperty val id: String,
                      @JsonProperty val subnets: List<String>) : Shareable


data class TerraformProvider(@JsonProperty("state_bucket") val stateBucket: String)


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
        @JsonProperty val source: String,
        @JsonProperty val default: Any? = null)


data class TfVariables(@get:JsonAnyGetter val map: Map<String, Any?> = emptyMap())

@JsonIgnoreProperties(value = *arrayOf("name"))
data class TfModule(@get:JsonIgnore
                    val name: String,

                    @JsonProperty
                    val source: String,

                    @JsonProperty
                    @JsonUnwrapped
                    val variables: TfVariables = TfVariables())

class TfTemplate(@JsonProperty("module") val modules: Map<String, TfModule>)