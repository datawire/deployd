package io.datawire.deployd.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.datawire.deployd.api.Workspace
import io.datawire.deployd.api.World
import java.nio.file.Files
import javax.ws.rs.*


@Path("/worlds")
class WorldsResource(workspace: Workspace, private val mapper: ObjectMapper) {

    val workspace: java.nio.file.Path = workspace.path.resolve("worlds")

    init {
        Files.createDirectories(this.workspace)
    }

    @GET
    @Path("/{name}")
    @Produces("application/json")
    fun getWorld(@PathParam("name") name: String): World {
        val world = loadWorld(name)

        // don't return credentials
        return world.copy(amazon = world.amazon.copy(accessKey = null, secretKey = null))
    }

    @POST
    @Consumes("application/json")
    fun createWorld(world: World) = storeWorld(world)

    private fun storeWorld(world: World) {
        mapper.writeValue(Files.createFile(workspace.resolve("${world.name}.json")).toFile(), world)
    }

    private fun loadWorld(name: String): World {
        return mapper.readValue(workspace.resolve("$name.json").toFile(), World::class.java)
    }
}