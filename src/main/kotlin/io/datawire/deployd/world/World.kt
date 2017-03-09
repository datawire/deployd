package io.datawire.deployd.world

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.persistence.Workspace
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.Shareable


data class World(@JsonProperty val name: String,
                 @JsonProperty val amazon: AwsProvider,
                 @JsonProperty val parameters: Map<String, String>) : Shareable

data class AwsProvider(@JsonProperty val accessKey: String?,
                       @JsonProperty val secretKey: String?,
                       @JsonProperty val region: String,
                       @JsonProperty val network: AwsNetwork,
                       @JsonProperty val s3StateStore: String) : Shareable

data class AwsNetwork(@JsonProperty val id: String,
                      @JsonProperty val subnets: List<String>) : Shareable


fun writeWorld(vertx: Vertx, world: World) {
    Workspace.writeFile(vertx, "world.json", Buffer.buffer(JsonObject.mapFrom(world).toString()))
}

fun loadWorld(vertx: Vertx): World {
    val buf = Workspace.readFile(vertx, "world.json")
    return buf.toJsonObject().mapTo(World::class.java)
}