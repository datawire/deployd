package io.datawire.md.service

import com.fasterxml.jackson.annotation.JsonProperty


data class ServiceRef(@JsonProperty val name: String,
                      @JsonProperty val version: String?)

