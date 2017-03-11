package io.datawire.md

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.deployd.service.Service
import io.datawire.md.service.validate
import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.AbstractVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext


private const val JSON_TYPE = "application/json"
private const val YAML_TYPE = "application/yaml"


class Api : AbstractVerticle() {

}

inline fun <reified T: Any> readBody(contentType: String, buffer: Buffer): T {
    val ct = contentType.toLowerCase()
    return when {
        ct.endsWith("json") -> ObjectMappers.mapper.readValue(buffer.toString())
        ct.endsWith("yaml") -> ObjectMappers.yamlMapper.readValue(buffer.toString())
        else                -> throw IllegalArgumentException("Cannot handle content type: '$contentType'")
    }
}

private fun configure(router: Router) {
    router.post("/services").apply {
        consumes(JSON_TYPE)
        consumes(YAML_TYPE)
        produces(JSON_TYPE)
        handler(::upsertService)
    }

    router.post("/services/:name/deploy").apply {
        consumes(JSON_TYPE)
        produces(JSON_TYPE)
        handler(::deployService)
    }
}

private fun upsertService(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val req = ctx.request()

    req.bodyHandler { body ->
        val service = readBody<Service>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)

        validate(vertx, service)
        vertx.eventBus().send<Service>("deploy.service:upsert", service) { reply ->
            if (reply.succeeded()) {

            } else {

            }
        }
    }
}

private fun deployService(ctx: RoutingContext) {
    val vertx = ctx.vertx()
    val req = ctx.request()

    req.bodyHandler { body ->
        val service = readBody<Service>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)

        validate(vertx, service)
        vertx.eventBus().send<Service>("deploy.service:deploy", service) {

        }
    }
}































