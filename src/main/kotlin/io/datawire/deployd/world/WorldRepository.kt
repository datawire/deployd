package io.datawire.deployd.world

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.shareddata.LocalMap


class WorldRepository internal constructor(private val backingMap: LocalMap<String, World>) {

    fun addWorld(world: World) {
        backingMap.putIfAbsent(world.name, world)
    }

    fun getWorld(name: String): World? = backingMap[name]

    fun getWorlds() = backingMap.values().toList()

    fun removeWorld(name: String) {
        backingMap.remove(name)
    }

    fun save(vertx: Vertx, path: String) {
        val json = ObjectMappers.prettyMapper.writeValueAsString(getWorlds())
        vertx.fileSystem().writeFileBlocking(path, Buffer.buffer(json))
    }

    companion object {

        @Volatile
        private var instance: WorldRepository? = null

        fun load(vertx: Vertx, path: String): WorldRepository {
            synchronized(this) {
                if (instance == null) {
                    instance = WorldRepository(vertx.sharedData().getLocalMap<String, World>("worlds"))
                    if (vertx.fileSystem().existsBlocking(path)) {
                        val buffer = vertx.fileSystem().readFileBlocking(path)
                        val worlds = ObjectMappers.mapper.readValue<List<World>>(buffer.toString(Charsets.UTF_8))
                        for (world in worlds) {
                            instance?.addWorld(world)
                        }
                    }
                }

                return instance!!
            }
        }
    }
}