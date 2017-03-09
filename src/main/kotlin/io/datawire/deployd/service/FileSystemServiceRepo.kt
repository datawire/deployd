package io.datawire.deployd.service

import io.datawire.deployd.persistence.Workspace
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject


class FileSystemServiceRepo(private val vertx: Vertx) {

    private val pathPrefix = "services/"

    fun addService(service: Service) {
        if (!exists(service.name)) {
            Workspace.createDirectories(vertx, "$pathPrefix/${service.name}")
            writeDescriptor(service)
            Workspace.writeFile(vertx, "$pathPrefix/${service.name}/terraform.tfvars.json", Buffer.buffer("{ }"))
        } else {
            throw IllegalStateException("Service['${service.name}'] already exists!")
        }
    }

    fun getService(serviceName: String): Service? {
        return if (exists(serviceName)) {
            val json = readDescriptor(serviceName)
            json.mapTo(Service::class.java)
        } else {
            null
        }
    }

    fun getServices(): List<Service> {
        val serviceNames = Workspace.listDirectories(vertx, pathPrefix)
        val descriptors  = serviceNames.map {
            readDescriptor(it).mapTo(Service::class.java)
        }

        return descriptors
    }

    private fun writeDescriptor(service: Service) {
        val descriptorPath = "$pathPrefix/${service.name}/service.json"
        val data = Buffer.buffer(JsonObject.mapFrom(service).toString())
        Workspace.writeFile(vertx, descriptorPath, data)
    }

    private fun readDescriptor(serviceName: String): JsonObject {
        val descriptorPath = "$pathPrefix/$serviceName/service.json"

        if (Workspace.contains(vertx, descriptorPath)) {
            return Workspace.readFile(vertx, descriptorPath).toJsonObject()
        } else {
            throw IllegalStateException("Service['$serviceName'] missing config!")
        }
    }

    private fun exists(serviceName: String) = Workspace.contains(vertx, "$pathPrefix/$serviceName")
}