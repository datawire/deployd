package io.datawire.md.service

import io.datawire.deployd.service.ServiceSpec

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.LoggerFactory


class ServicePersistence(vertx: Vertx) {

    private val logger = LoggerFactory.getLogger(ServicePersistence::class.java)
    private val workspacePrefix = "services/"

    private val workspace = io.datawire.md.core.Workspace(vertx)

    fun putFile(service: ServiceRef, path: String, data: Buffer) {

    }

    fun putService(service: ServiceSpec) =
            workspace.writeYamlFileBlocking("$workspacePrefix/${service.name}/service.yaml", service)

    fun getServiceByName(name: String): ServiceSpec? {
        return try {
            workspace.readYamlFileBlocking("$workspacePrefix/$name/service.yaml")
        } catch (fse: FileSystemException) {
            logger.error("Unable to read service specification (name: $name)", fse)
            null
        }
    }

    fun listServices(): List<ServiceSpec> {
        return workspace.listDirectories(workspacePrefix).map { workspace.readYamlFileBlocking<ServiceSpec>(it + "/service.yaml") }
    }
}