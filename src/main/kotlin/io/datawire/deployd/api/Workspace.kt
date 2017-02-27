package io.datawire.deployd.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.nio.file.Files
import java.nio.file.Path


data class Workspace(@JsonProperty val path: Path) {

    val worldsWorkspace:   Path = path.resolve("worlds")
    val servicesWorkspace: Path = path.resolve("services")

    fun initialize() = listOf(worldsWorkspace, servicesWorkspace).forEach { Files.createDirectories(it) }
}