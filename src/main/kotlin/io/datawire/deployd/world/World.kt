package io.datawire.deployd.world

import com.fasterxml.jackson.annotation.JsonProperty
import io.vertx.core.shareddata.Shareable


data class World(@JsonProperty val name: String,
                 @JsonProperty val amazon: AwsProvider) : Shareable

data class AwsProvider(@JsonProperty val accessKey: String?,
                       @JsonProperty val secretKey: String?,
                       @JsonProperty val region: String,
                       @JsonProperty val network: AwsNetwork) : Shareable

data class AwsNetwork(@JsonProperty val id: String,
                      @JsonProperty val subnets: List<String>) : Shareable