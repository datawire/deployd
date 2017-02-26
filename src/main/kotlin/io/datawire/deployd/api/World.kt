package io.datawire.deployd.api

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.cloud.AwsProvider


data class World(@JsonProperty val name: String,
                 @JsonProperty val description: String,
                 @JsonProperty val amazon: AwsProvider, // TODO: abstract this to a generic providers collection
                 @JsonProperty val terraform: String)