package io.datawire.deployd.api

import com.fasterxml.jackson.annotation.JsonProperty


data class ServiceMetadata(@JsonProperty val name: String)