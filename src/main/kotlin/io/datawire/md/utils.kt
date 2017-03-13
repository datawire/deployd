package io.datawire.md

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.deployd.api.collectionResult
import io.datawire.vertx.json.ObjectMappers.mapper
import io.datawire.vertx.json.ObjectMappers.prettyMapper
import io.datawire.vertx.json.ObjectMappers.yamlMapper
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.nio.charset.Charset


fun toBuffer(data: String, charset: Charset = Charsets.UTF_8): Buffer = Buffer.buffer(data, charset.name())

inline fun <reified T: Any> fromJson(buffer: Buffer): T = fromJson(buffer.toString(Charsets.UTF_8))
inline fun <reified T: Any> fromJson(string: String): T = mapper.readValue<T>(string)

inline fun <reified T: Any> fromYaml(buffer: Buffer): T = fromYaml(buffer.toString(Charsets.UTF_8))
inline fun <reified T: Any> fromYaml(string: String): T = yamlMapper.readValue<T>(string)

fun toJson(any: Any): String = prettyMapper.writeValueAsString(any)
fun toYaml(any: Any): String = yamlMapper.writeValueAsString(any)

fun toJsonObject(any: Any): JsonObject = JsonObject.mapFrom(any)


fun jsonCollectionResponse(ctx: RoutingContext,
                           collectionName: String,
                           collection: Collection<Any>,
                           headers: Map<String, String> = emptyMap()) {

    val result = collectionResult(collectionName, collection)
    with(ctx.response()) {
        headers.forEach { (name, value) -> putHeader(name, value) }
        putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        end(result)
    }
}

fun jsonResponse(ctx: RoutingContext, item: Any, headers: Map<String, String> = emptyMap()) {
    with(ctx.response()) {
        headers.forEach { (name, value) -> putHeader(name, value) }
        putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        end(toJson(item))
    }
}

fun notFoundResponse(response: HttpServerResponse, headers: Map<String, String> = emptyMap()) {
    with(response) {
        statusCode = HttpResponseStatus.NOT_FOUND.code()
        headers.forEach { (name, value) -> putHeader(name, value) }
        end()
    }
}

fun noContentResponse(response: HttpServerResponse, headers: Map<String, String> = emptyMap()) {
    with(response) {
        statusCode = HttpResponseStatus.NO_CONTENT.code()
        headers.forEach { (name, value) -> putHeader(name, value) }
        end()
    }
}