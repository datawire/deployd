package io.datawire.md.fabric

import com.fasterxml.jackson.annotation.*
import io.datawire.md.deploy.terraform.TfProvider
import io.vertx.core.shareddata.Shareable


data class FabricSpec(@JsonProperty val name: String,
                      @JsonProperty val parameters: Map<String, *>,
                      @JsonProperty val amazon: AwsProvider,
                      @JsonProperty val terraform: TerraformProvider)


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = TfModuleSpec::class, name = "terraform")
)
abstract class ModuleSpec(
        @JsonProperty
        open val name: String,

        @JsonProperty
        open val version: Int
) {
    val id get() = "$name-v$version"
}

//data class TerraformModuleSpec(
//        override val name: String,
//        override val version: Int,
//
//        @JsonProperty
//        val source: String,
//
//        @JsonProperty
//        val inputMappings: Map<String, TfVariableSpecOld> = emptyMap(),
//
//        @JsonProperty
//        val outputMappings: Map<String, String> = emptyMap()) : ModuleSpec(name, version)


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = AwsProvider::class, name = "aws")
)
interface Provider


data class AwsProvider(@JsonProperty("access_key") val accessKey: String?,
                       @JsonProperty("secret_key") val secretKey: String?,
                       @JsonProperty("region")     val region: String) : Provider {

    fun toTerraformProvider(): TfProvider {

        // TODO: use environment vars for the secrets in the future
        return TfProvider(name = "aws", params = mapOf(
                "access_key" to accessKey!!,
                "secret_key" to secretKey!!,
                "region" to region
        ))
    }
}


data class TerraformProvider(@JsonProperty("state_bucket") val stateBucket: String)


data class Parameters(private val map: Map<String, Any?>): Map<String, Any?> by map


data class PlanningContext(val deployId     : String,
                           val moduleSpec   : TfModuleSpec,
                           val parameters   : Parameters)


data class TfModuleSpec(@JsonProperty val name    : String,
                        @JsonProperty val version : Int,
                        @JsonProperty val source  : String,
                        @JsonProperty val inputs  : Map<String, TfVariableSpecOld>,
                        @JsonProperty val outputs : Map<String, String>) {

    val id get() = "${name.toLowerCase()}-v$version"
}

data class TfVariableSpecOld(
        @get:JsonIgnoreProperties val name: String,
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

class TfTemplateOld(@JsonProperty("module") val modules: Map<String, TfModule>)