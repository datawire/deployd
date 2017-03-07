package io.datawire.deployd

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.deployd.service.Service
import io.datawire.deployd.world.World
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.Shareable

interface Identifiable : Shareable {
    val id: String
}

public typealias ServiceRepo = LocalMapRepository<Service>
public typealias WorldRepo = LocalMapRepository<World>

class LocalMapRepository<T : Identifiable> constructor(private val localMap: LocalMap<String, T>) {

    fun contains(key: String) = localMap.get(key) != null

    fun get(key: String): T? = localMap[key]

    fun getAll(): Collection<T> = localMap.values() ?: emptyList()

    fun add(item: T) = localMap.putIfAbsent(item.id, item) == null

    fun remove(key: String) = localMap.remove(key) != null

    fun store(vertx: Vertx, path: String) {
        val json = ObjectMappers.prettyMapper.writeValueAsString(getAll())
        vertx.fileSystem().writeFileBlocking(path, Buffer.buffer(json))
    }

    companion object {
        inline fun <reified T : Identifiable> getInstance(vertx: Vertx, name: String): LocalMapRepository<T> {
            val backingMap = vertx.sharedData().getLocalMap<String, T>(name)
            if (backingMap.isEmpty && vertx.fileSystem().existsBlocking(".deployd/$name.json")) {
                val json = vertx.fileSystem().readFileBlocking(".deployd/$name.json").toString(Charsets.UTF_8)
                ObjectMappers.mapper.readValue<List<T>>(json).forEach { backingMap.put(it.id, it) }
            }

            return LocalMapRepository(backingMap)
        }
    }
}
