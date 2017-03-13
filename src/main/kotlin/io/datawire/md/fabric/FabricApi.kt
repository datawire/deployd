package io.datawire.md.fabric

import io.datawire.md.*
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext


object FabricApi {

    fun putFabric(ctx: RoutingContext) {
        val req = ctx.request()
        val fabricStore = FabricPersistence(ctx.vertx())

        req.bodyHandler { body ->
            val fabricSpec = readBody<FabricSpec>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)
            fabricStore.putFabric(fabricSpec)
            noContentResponse(ctx.response())
        }
    }

    fun getFabric(ctx: RoutingContext) {
        val fabricStore = FabricPersistence(ctx.vertx())
        val fabric = fabricStore.getFabric()

        fabric?.let {
            jsonResponse(ctx, it.copy(amazon = it.amazon.copy(secretKey = null))) } ?: notFoundResponse(ctx.response())
    }

    fun getModule(ctx: RoutingContext) {
        val fabricStore = FabricPersistence(ctx.vertx())
        val module = fabricStore.getModuleById(ctx.pathParam("id"))
        module?.let { jsonResponse(ctx, it)} ?: notFoundResponse(ctx.response())
    }

    fun listModules(ctx: RoutingContext) {
        val fabricStore = FabricPersistence(ctx.vertx())
        val modules = fabricStore.listModules()
        jsonCollectionResponse(ctx, "modules", modules)
    }

    fun putModule(ctx: RoutingContext) {
        val req = ctx.request()
        val fabricStore = FabricPersistence(ctx.vertx())

        req.bodyHandler { body ->
            val fabricSpec = readBody<ModuleSpec>(req.getHeader(HttpHeaders.CONTENT_TYPE), body)
            fabricStore.putModule(fabricSpec)
            noContentResponse(ctx.response())
        }
    }
}
