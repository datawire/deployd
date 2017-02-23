package io.datawire.deployd.api

import com.fasterxml.jackson.annotation.JsonProperty


data class DockerRepository(@JsonProperty val registry: String,
                            @JsonProperty val image: String,
                            @JsonProperty val tag: String)