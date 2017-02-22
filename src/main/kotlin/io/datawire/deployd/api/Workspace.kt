package io.datawire.deployd.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.nio.file.Path


data class Workspace(@JsonProperty val path: Path)