package io.datawire.deployd.cloud

import com.fasterxml.jackson.annotation.JsonProperty


data class AwsProvider(@JsonProperty val accessKey: String?,
                       @JsonProperty val secretKey: String?,
                       @JsonProperty val region: String)