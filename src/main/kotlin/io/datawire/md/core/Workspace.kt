package io.datawire.md.core

import io.datawire.deployd.WorkspaceConfig
import io.datawire.md.*
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths


class Workspace(private val vertx: Vertx) {

    private val logger = LoggerFactory.getLogger(Workspace::class.java)

    private val fs get() = vertx.fileSystem()

    private val config = loadConfig(vertx)

    private fun loadConfig(vertx: Vertx): WorkspaceConfig {
        val configStore = vertx.sharedData().getLocalMap<String, String>("deployd.config")
        val config = configStore["workspace"]

        return config?.let { fromJson<WorkspaceConfig>(it) } ?:
                throw IllegalStateException("deploy.config['workspace'] not found!")
    }

    private fun resolvePath(path: String) = config.path.resolve(path).toAbsolutePath().toString()

    fun delete(path: String, recursive: Boolean = true) {
        val resolvedPath = resolvePath(path)
        fs.deleteRecursiveBlocking(resolvedPath, recursive)
        logger.debug("deleted file/directory '{}'", resolvedPath)
    }

    fun listDirectories(path: String, stripPath: Boolean = false): List<String> {
        val directories = fs.readDirBlocking(resolvePath(path))
        if (stripPath) {
            directories.map { it.substring(it.lastIndexOf('/') + 1) }
        }

        return directories
    }

    fun listFiles(path: String, stripPath: Boolean = false): List<String> {
        val files = fs.readDirBlocking(resolvePath(path))
        if (stripPath) {
            files.map { it.substring(it.lastIndexOf('/') + 1) }
        }

        return files
    }

    fun fileExists(path: String): Boolean {
        val resolvedPath = resolvePath(path)
        val result = fs.existsBlocking(resolvedPath) && Files.isRegularFile(Paths.get(resolvedPath))
        logger.debug("checked if file '{}' exists (result: {})", resolvedPath, result)

        return result
    }

    fun directoryExists(path: String): Boolean {
        val resolvedPath = resolvePath(path)
        val result = fs.existsBlocking(resolvedPath) && Files.isDirectory(Paths.get(resolvedPath))
        logger.debug("checked if directory '{}' exists (result: {})", resolvedPath, result)
        return result
    }

    fun writeFileBlocking(path: String, data: Buffer) {
        val resolvedPath = resolvePath(path)
        fs.writeFileBlocking(resolvedPath, data)
        logger.debug("wrote content to file '{}'", path)
    }

    fun writeYamlFileBlocking(path: String, any: Any) = writeFileBlocking(path, toBuffer(toYaml(any)))
    fun writeJsonFileBlocking(path: String, any: Any) = writeFileBlocking(path, toBuffer(toJson(any)))

    fun readFileBlocking(path: String): Buffer {
        val resolvedPath = resolvePath(path)
        logger.debug("read content from file '{}'", path)
        return fs.readFileBlocking(resolvedPath)
    }

    inline fun <reified T: Any> readYamlFileBlocking(path: String) = fromYaml<T>(readFileBlocking(path))
    inline fun <reified T: Any> readJsonFileBlocking(path: String) = fromJson<T>(readFileBlocking(path))
}