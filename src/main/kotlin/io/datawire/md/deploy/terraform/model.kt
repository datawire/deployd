package io.datawire.md.deploy.terraform

import com.fasterxml.jackson.annotation.*
import io.datawire.md.fabric.ModuleSpec


data class TfRemoteStateConfig(@JsonProperty val bucket: String,
                               @JsonProperty val name: String,
                               @JsonProperty val region: String)


data class TfApplyContext(val modulePath: String, val planPath: String)


data class TfPlanContext(
        val remoteState : TfRemoteStateConfig,
        val providers   : Map<String, TfProvider>,
        val modulePath  : String,
        val destroy     : Boolean = false)

enum class TfVariableType {
    @JsonProperty("string")
    STRING,

    @JsonProperty("list")
    LIST,

    @JsonProperty("map")
    MAP
}

data class TfGenerateModuleContext(
        @JsonProperty
        val moduleName: String,

        @JsonProperty
        val moduleSpec: TfModuleSpec,

        @JsonProperty
        val mappableParameters: Map<String, Any?>)

data class TfModuleSpec(@JsonProperty override val name   : String,
                        @JsonProperty override val version: Int,
                        @JsonProperty val source : String,
                        @JsonProperty val inputs : List<TfVariableSpec>,
                        @JsonProperty val outputs: List<TfOutputSpec>) : ModuleSpec(name, version)

data class TfOutputSpec(
        @JsonProperty val name: String,
        @JsonProperty val target: String)

data class TfVariableSpec(
        @JsonProperty val target  : String,
        @JsonProperty val type    : TfVariableType,
        @JsonProperty val source  : String,
        @JsonProperty val default : Any? = null)

data class TfTemplate(
        @JsonProperty("provider") val providers: Map<String, TfProvider> = emptyMap(),
        @JsonProperty("module") val modules: Map<String, TfModule> = emptyMap(),
        @JsonProperty("output") val outputs: Map<String, TfOutput> = emptyMap())

data class TfProvider(
        @get:JsonIgnore val name: String,
        @get:JsonAnyGetter val params: Map<String, String>)


data class TfModule(
        @get:JsonIgnore
        val name: String,

        @JsonProperty
        val source: String,

        @get:JsonAnyGetter
        val inputs: Map<String, Any>)

data class TfOutput(
        @get:JsonIgnore val name: String,
        @JsonProperty val value: Any?)


