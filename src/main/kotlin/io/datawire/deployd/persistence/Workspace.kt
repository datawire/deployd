package io.datawire.deployd.persistence

import io.datawire.md.WorkspaceConfig
import io.datawire.md.toJson
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import java.nio.file.Paths


object Workspace {

    fun initialize(vertx: Vertx, config: WorkspaceConfig) {
        val configMap = vertx.sharedData().getLocalMap<String, JsonObject>("md.config")
        configMap.putIfAbsent("workspace", JsonObject.mapFrom(config))

        val configMap2 = vertx.sharedData().getLocalMap<String, String>("deployd.config")
        configMap2.putIfAbsent("workspace", toJson(config))


        if (!vertx.fileSystem().existsBlocking(config.path.toString())) {
            vertx.fileSystem().mkdirBlocking(config.path.toString())
        }

        createDirectories(vertx, Paths.get("services").toString())
        createDirectories(vertx, Paths.get("modules").toString())
    }

    fun path(vertx: Vertx): String {
        val config = getConfig(vertx)
        return config.path.toString()
    }

    fun contains(vertx: Vertx, path: String): Boolean {
        val config = getConfig(vertx)
        val checkFor = config.path.resolve(path).toString()
        return vertx.fileSystem().existsBlocking(checkFor)
    }

    fun readFile(vertx: Vertx, path: String): Buffer {
        val config = getConfig(vertx)
        val readFrom = config.path.resolve(path).toString()
        return vertx.fileSystem().readFileBlocking(readFrom)
    }

    private fun getConfig(vertx: Vertx): WorkspaceConfig {
        val configMap = vertx.sharedData().getLocalMap<String, JsonObject>("md.config")
        return configMap["workspace"]!!.mapTo(WorkspaceConfig::class.java)
    }

    fun listDirectories(vertx: Vertx, path: String): List<String> {
        val config = getConfig(vertx)
        val readFrom = config.path.resolve(path).toString()
        val dirs = vertx.fileSystem().readDirBlocking(readFrom)
        return dirs.map { Paths.get(it).fileName.toString() }
    }

    fun listFiles(vertx: Vertx, path: String): List<String> {
        val config = getConfig(vertx)
        val readFrom = config.path.resolve(path).toString()
        val dirs = vertx.fileSystem().readDirBlocking(readFrom)
        return dirs.map { Paths.get(it).fileName.toString() }
    }

    fun writeFile(vertx: Vertx, path: String, data: Buffer) {
        val config = getConfig(vertx)
        val writeTo = config.path.resolve(path).toString()
        vertx.fileSystem().writeFileBlocking(writeTo, data)

    }

    fun createFile(vertx: Vertx, path: String) {
        val config = getConfig(vertx)
        val create = config.path.resolve(path).toString()
        if (!vertx.fileSystem().existsBlocking(create)) {
            vertx.fileSystem().createFileBlocking(create)
        }
    }

    fun createDirectories(vertx: Vertx, path: String) {
        val config = getConfig(vertx)
        val create = config.path.resolve(path).toString()
        if (!vertx.fileSystem().existsBlocking(create)) {
            vertx.fileSystem().mkdirBlocking(create)
        }
    }

    fun deleteDirectories(vertx: Vertx, path: String) {
        val config = getConfig(vertx)
        val delete = config.path.resolve(path).toString()
        if (vertx.fileSystem().existsBlocking(delete)) {
            vertx.fileSystem().deleteRecursiveBlocking(delete, true)
        }
    }
}