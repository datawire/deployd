package io.datawire.md.service

import io.datawire.deployd.service.ServiceSpec
import io.datawire.md.*
import io.datawire.md.deploy.Deployment
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext


object ServiceApi {

    fun getService(ctx: RoutingContext) {
        val serviceStore = ServicePersistence(ctx.vertx())
        val module = serviceStore.getServiceByName(ctx.pathParam("id"))
        module?.let { jsonResponse(ctx, it) } ?: notFoundResponse(ctx.response())
    }

    fun listServices(ctx: RoutingContext) {
        val serviceStore = ServicePersistence(ctx.vertx())
        val services = serviceStore.listServices()
        jsonCollectionResponse(ctx, "services", services)
    }

    fun upsertService(ctx: RoutingContext) {
        val req = ctx.request()
        req.bodyHandler { body ->
            val service = readBody<ServiceSpec>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)
            validate(ctx.vertx(), service)
            ctx.vertx().eventBus().send<Deployment>("deploy.service:upsert", service) { reply ->
                if (reply.succeeded()) {
                    jsonResponse(ctx, reply.result().body())
                } else {
                    ctx.fail(reply.cause())
                }
            }
        }
    }

    private fun deploy(ctx: RoutingContext) {
        val vertx = ctx.vertx()
        val req = ctx.request()

        req.bodyHandler { body ->
            val service = readBody<ServiceSpec>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)

            validate(vertx, service)
            vertx.eventBus().send<ServiceSpec>("deploy.service:deploy", service) {

            }
        }
    }
}