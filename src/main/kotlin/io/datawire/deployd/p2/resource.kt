package io.datawire.deployd.p2

import javax.ws.rs.*
import javax.ws.rs.core.Response.*


@Path("/deploys")
class DeploymentsResource(val services: ServiceRepo) {

  @POST
  @Path("/{service}")
  @Consumes("application/json")
  fun deploy(@PathParam("service")
             serviceName: String,

             request: DeploymentRequest) {

    val service = services[serviceName] ?: throw WebApplicationException(Status.NOT_FOUND)

    //val deploymentRequest = DeploymentRequest()
  }

}

@Path("/worlds")
class WorldsResource(private val repo: WorldRepo) {

  @POST
  fun addWorld(world: World) {
    repo.add(world)
  }

  @DELETE
  @Path("/{name}")
  fun removeWorld(@PathParam("name") worldName: String) {
    repo.remove(worldName)
  }

  @GET
  @Path("/{name}")
  fun getWorld(@PathParam("name") worldName: String) = repo.getByName(worldName)

  @GET
  fun listWorlds() = repo.getAll()

}

@Path("/services")
class ServicesResource {

  @POST
  fun addService(service: Service) {

  }

  @DELETE
  @Path("/{name}")
  fun removeService(@PathParam("name") serviceName: String) {

  }
  
  @GET
  @Path("/{name}")
  fun getService(@PathParam("name") serviceName: String): Service? {
    return null
  }
}