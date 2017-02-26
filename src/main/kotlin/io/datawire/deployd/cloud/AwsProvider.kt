package io.datawire.deployd.cloud

import com.fasterxml.jackson.annotation.JsonProperty


data class AwsProvider(@JsonProperty val accessKey: String?,
                       @JsonProperty val secretKey: String?,
                       @JsonProperty val region: String,
                       @JsonProperty val network: AwsNetwork) {

    fun toTerraformProvider(): Map<String, Any?> {
        return mapOf("aws" to mapOf(
                "region" to region,
                "accessKey" to accessKey,
                "secretKey" to secretKey
        ))
    }
}

data class AwsNetwork(@JsonProperty val id: String,
                      @JsonProperty val subnets: List<String>)