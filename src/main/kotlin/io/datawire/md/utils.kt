package io.datawire.md

import com.fasterxml.jackson.module.kotlin.readValue
import io.datawire.vertx.json.ObjectMappers.mapper
import io.datawire.vertx.json.ObjectMappers.yamlMapper
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import java.nio.charset.Charset


fun toBuffer(data: String, charset: Charset = Charsets.UTF_8) = Buffer.buffer(data, charset.name())

inline fun <reified T: Any> fromJson(buffer: Buffer): T = fromJson(buffer.toString(Charsets.UTF_8))
inline fun <reified T: Any> fromJson(string: String): T = mapper.readValue<T>(string)

inline fun <reified T: Any> fromYaml(buffer: Buffer): T = fromJson(buffer.toString(Charsets.UTF_8))
inline fun <reified T: Any> fromYaml(string: String): T = yamlMapper.readValue<T>(string)

fun toJson(any: Any): String = mapper.writeValueAsString(any)
fun toYaml(any: Any): String = yamlMapper.writeValueAsString(any)

fun toJsonObject(any: Any): JsonObject = JsonObject.mapFrom(any)
