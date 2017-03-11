package io.datawire.deployd.fabric

import io.datawire.deployd.api.collectionResult
import io.datawire.deployd.api.fromJson
import io.datawire.deployd.api.fromYaml
import io.datawire.deployd.world.World
import io.datawire.deployd.world.writeWorld
import io.datawire.md.fabric.TfModuleSpec
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext


private const val URL_BASE = "/fabric"

fun configure(router: Router) {

    with(router.post(URL_BASE)) {
        consumes("application/json")
        produces("application/json")
        handler(::registerFabric)
    }

    with(router.post("/fabric/modules")) {
        consumes("application/json")
        consumes("application/yaml")
        produces("application/json")
        handler(::registerModule)
    }

    with(router.get("/fabric/modules")) {
        produces("application/json")
        handler(::listModules)
    }
}

fun registerFabric(ctx: RoutingContext) {
    val fabric = ctx.bodyAsJson.mapTo(World::class.java)
    writeWorld(ctx.vertx(), fabric)
    ctx.response().apply {
        statusCode = 204
        end()
    }
}

fun registerModule(ctx: RoutingContext) {
    val req  = ctx.request()
    val module = when(req.getHeader(HttpHeaders.CONTENT_TYPE)) {
        "application/json" -> fromJson<TfModuleSpec>(ctx.bodyAsString)
        "application/yaml" -> fromYaml<TfModuleSpec>(ctx.bodyAsString)
        else -> throw RuntimeException("Unknown Content-Type: ${req.getHeader(HttpHeaders.CONTENT_TYPE)}")
    }

    addModule(ctx.vertx(), module)

    val resp = ctx.response()
    resp.setStatusCode(HttpResponseStatus.ACCEPTED.code()).end()
}


fun listModules(ctx: RoutingContext) {
    val modules = getModules(ctx.vertx())
    ctx.response().apply {
        statusCode = 200
        putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        end(collectionResult("modules", modules))
    }
}
