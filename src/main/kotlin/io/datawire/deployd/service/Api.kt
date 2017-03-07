package io.datawire.deployd.service

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.deployd.ServiceRepo
import io.datawire.deployd.api.Api
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

object ServicesApi : Api {

    override fun configure(router: Router) {
        with(router.post("/services")) {
            consumes("application/json")
            produces("application/json")
            handler(this@ServicesApi::addService)
        }

        with(router.get("/services/:id")) {
            produces("application/json")
            handler(this@ServicesApi::getService)
        }

        with(router.get("/services")) {
            produces("application/json")
            handler(this@ServicesApi::getServices)
        }
    }

    private fun addService(ctx: RoutingContext) {
        val resp = ctx.response()

        val service = ObjectMappers.mapper.readValue<Service>(ctx.bodyAsString)
        val serviceRepo = ServiceRepo.getInstance<Service>(ctx.vertx(), "services.json")

        serviceRepo.add(service)

        resp.apply {
            statusCode = 200
            putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            end(result(service))
        }    
    }

    private fun getService(ctx: RoutingContext) {
        val resp = ctx.response()

        val serviceRepo = ServiceRepo.getInstance<Service>(ctx.vertx(), "services.json")
        val service = serviceRepo.get(ctx.pathParam("name"))

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

        val serviceRepo = ServiceRepo.getInstance<Service>(ctx.vertx(), "services.json")
        val services = serviceRepo.getAll()

        resp.apply {
            statusCode = 200
            putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            end(collectionResult("services", services))
        }
    }
}