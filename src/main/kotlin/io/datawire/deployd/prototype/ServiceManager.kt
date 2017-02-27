package io.datawire.deployd.prototype

import io.datawire.deployd.api.Service
import io.datawire.deployd.api.World
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path


class ServiceManager(private val world: World) {

    fun setupWorkspace(workspace: Path, service: Service) {
        val path = Files.createDirectories(workspace.resolve(service.name))

        initializeWorkspace(path)
    }

    fun initializeWorkspace(workspace: Path) {
        val terraformEntrypoint = workspace.resolve("main.tf")

        try {
            Files.createFile(terraformEntrypoint)
        } catch (exists: FileAlreadyExistsException) { /* not important if file already exists */ }


    }
}