package io.datawire.deployd.service

import io.datawire.deployd.api.Api
import io.datawire.deployd.api.fromJson
import io.datawire.deployd.api.fromYaml
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

object ServicesApi : Api {

    override fun configure(router: Router) {
        with(router.post("/services")) {
            consumes("application/json")
            consumes("application/yaml")
            produces("application/json")
            handler(this@ServicesApi::addService)
        }

        with(router.post("/services/:name/deploy")) {
            handler({ it.reroute("/deployments/${it.pathParam("name")}") })
        }

        with(router.get("/services/:name")) {
            produces("application/json")
            handler(this@ServicesApi::getService)
        }

        with(router.get("/services")) {
            produces("application/json")
            handler(this@ServicesApi::getServices)
        }
    }

    private fun validate(service: Service) {
        for (fe in service.network.frontends) {
            checkFrontendToBackendPortMapping(fe, service.network.backends)
        }
    }

    private fun addService(ctx: RoutingContext) {
        val req  = ctx.request()
        val resp = ctx.response()

        val service = when(req.getHeader(HttpHeaders.CONTENT_TYPE)) {
            "application/json" -> fromJson<Service>(ctx.bodyAsString)
            "application/yaml" -> fromYaml(ctx.bodyAsString)
            else -> throw RuntimeException("Unknown Content-Type: ${req.getHeader(HttpHeaders.CONTENT_TYPE)}")
        }

        val serviceRepo = FileSystemServiceRepo(ctx.vertx()) //LocalMapServiceRepo.get(ctx.vertx())
        serviceRepo.addService(service)

        // TODO: this will need to throw a better exception type that can be mapped to an API error
        validate(service)

        ctx.vertx().eventBus().send("deploy.ServiceSetup", service)

        resp.apply {
            statusCode = 200
            putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            end(result(service))
        }    
    }

    private fun getService(ctx: RoutingContext) {
        val resp = ctx.response()

        val serviceRepo = FileSystemServiceRepo(ctx.vertx()) //LocalMapServiceRepo.get(ctx.vertx())
        val service = serviceRepo.getService(ctx.pathParam("name"))

        if (service != null) {
            resp.setStatusCode(200)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(result(service))
        } else {
            resp.setStatusCode(404).end()
        }
    }

    private fun getServices(ctx: RoutingContext) {
        val resp = ctx.response()

        val serviceRepo = FileSystemServiceRepo(ctx.vertx()) //LocalMapServiceRepo.get(ctx.vertx())
        val services = serviceRepo.getServices()

        resp.apply {
            statusCode = 200
            putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            end(collectionResult("services", services))
        }
    }
}