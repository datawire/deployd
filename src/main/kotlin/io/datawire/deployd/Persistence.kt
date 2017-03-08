package io.datawire.deployd

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.deployd.service.Service
import io.datawire.deployd.world.World
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystem
import io.vertx.core.shareddata.LocalMap

// WARNING! This entire concept is an abomination that needs to be removed once we move beyond Alpha


abstract class LocalMapRepo<T>(private val storagePath: String, val localMap: LocalMap<String, T>) {

    operator fun contains(key: String) = localMap.get(key) != null

    fun get(key: String): T? = localMap[key]

    fun getAll(): Collection<T> = localMap.values() ?: emptyList()

    fun add(key: String, item: T) = localMap.putIfAbsent(key, item) == null

    fun remove(key: String) = localMap.remove(key) != null

    fun save(fs: FileSystem) {
        val mapper = ObjectMappers.prettyMapper
        fs.writeFileBlocking(storagePath, Buffer.buffer(mapper.writeValueAsString(getAll())))
    }
}

class LocalMapServiceRepo(storagePath: String, localMap: LocalMap<String, Service>) : LocalMapRepo<Service>(storagePath, localMap) {
    companion object {
        private val mapName = "services"
        private val storage = "$mapName.json"

        fun get(vertx: Vertx): LocalMapServiceRepo {
            val fs = vertx.fileSystem()
            val mapper = ObjectMappers.mapper

            val map = vertx.sharedData().getLocalMap<String, Service>(mapName)
            val data = if (fs.existsBlocking(".deployd/$storage")) {
                fs.readFileBlocking(".deployd/$storage").toString("UTF-8")
            } else {
                "[ ]"
            }

            mapper.readValue<List<Service>>(data).forEach { map.put(it.name, it) }
            return LocalMapServiceRepo(".deployd/$storage", map)
        }
    }
}


class LocalMapWorldRepo(storagePath: String, localMap: LocalMap<String, World>) : LocalMapRepo<World>(storagePath, localMap) {
    companion object {
        private val mapName = "worlds"
        private val storage = "$mapName.json"

        fun get(vertx: Vertx): LocalMapWorldRepo {
            val fs = vertx.fileSystem()
            val mapper = ObjectMappers.mapper

            val map = vertx.sharedData().getLocalMap<String, World>(mapName)
            val data = if (fs.existsBlocking(".deployd/$storage")) {
                fs.readFileBlocking(".deployd/$storage").toString("UTF-8")
            } else {
                "[ ]"
            }

            mapper.readValue<List<World>>(data).forEach { map.put(it.name, it) }
            return LocalMapWorldRepo(".deployd/$storage", map)
        }
    }
}
