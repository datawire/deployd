package io.datawire.md.fabric

import io.datawire.deployd.persistence.Workspace
import io.datawire.md.fabric.TfModuleSpec
import io.datawire.md.fromYaml
import io.datawire.md.toBuffer
import io.datawire.md.toYaml
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("fabric.persistence")


private val workspacePrefix = "modules/"


fun readFabric(vertx: Vertx): FabricSpec? {
    val fsPath = "fabric.yaml"
    if (Workspace.contains(vertx, fsPath)) {
        val data = Workspace.readFile(vertx, fsPath)
        return fromYaml(data)
    } else {
        return null
    }
}

fun putFabric(vertx: Vertx, spec: FabricSpec) {
    val fsPath = "fabric.yaml"
    Workspace.writeFile(vertx, fsPath, toBuffer(toYaml(spec)))
}

fun addModule(vertx: Vertx, module: TfModuleSpec): Boolean {
    val file = "$workspacePrefix/${module.id}.json"
    if (lookupModule(vertx, module.id) != null) {
        LOGGER.info("Module already exists (id: ${module.id})")
        return false
    }

    val json = JsonObject.mapFrom(module).toString()
    Workspace.writeFile(vertx, file, Buffer.buffer(json))
    return true
}

fun getModules(vertx: Vertx): List<TfModuleSpec> {
    val moduleFiles = Workspace.listFiles(vertx, workspacePrefix)
    return moduleFiles.map { Workspace.readFile(vertx, it).toJsonObject().mapTo(TfModuleSpec::class.java) }
}

fun lookupModule(vertx: Vertx, id: String): TfModuleSpec? {
    val file = "${workspacePrefix}/$id.json"
    return if (Workspace.contains(vertx, file)) {
        Workspace.readFile(vertx, file).toJsonObject().mapTo(TfModuleSpec::class.java)
    } else {
        null
    }
}