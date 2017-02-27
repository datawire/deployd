package io.datawire.deployd.api

import com.fasterxml.jackson.annotation.JsonProperty


data class Service(@JsonProperty val world: String, @JsonProperty val name: String)