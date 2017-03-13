package io.datawire.md.fabric


import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory


class FabricPersistence(vertx: Vertx) {

    private val logger = LoggerFactory.getLogger(FabricPersistence::class.java)
    private val workspacePrefix = "modules/"

    private val workspace = io.datawire.md.core.Workspace(vertx)

    fun getFabric(): FabricSpec? {
        return try {
            workspace.readYamlFileBlocking("fabric.yaml")
        } catch (fse: FileSystemException) {
            logger.error("Unable to read fabric information", fse)
            null
        }
    }

    fun putFabric(fabric: FabricSpec) = workspace.writeYamlFileBlocking("fabric.yaml", fabric)

    fun putModule(module: ModuleSpec) = workspace.writeYamlFileBlocking("$workspacePrefix/${module.id}.yaml", module)

    fun getModuleById(id: String): ModuleSpec? {
        return try {
            workspace.readYamlFileBlocking("$workspacePrefix/$id.yaml")
        } catch (fse: FileSystemException) {
            logger.error("Unable to read fabric module information", fse)
            null
        }
    }

    fun listModules(): List<ModuleSpec> {
        return workspace.listFiles(workspacePrefix).map { workspace.readYamlFileBlocking<ModuleSpec>(it) }
    }
}
