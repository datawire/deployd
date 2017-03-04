/*
 * Copyright 2017 Datawire. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.datawire.vertx.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * Factory for producing instances of the Jackson framework [ObjectMapper].
 *
 * @author plombardi@datawire.io
 */


object ObjectMappers {

    /**
     * JSON mapper that has support for Kotlin.
     */
    val mapper: ObjectMapper by lazy { configure(Json.mapper.copy()) }

    /**
     * JSON mapper that has support for Kotlin and outputs "pretty" indentation.
     */
    val prettyMapper: ObjectMapper by lazy { configure(Json.prettyMapper.copy()) }

    /**
     * Registers modules needed for this application with the provided [ObjectMapper] instance.
     *
     * @return the [ObjectMapper] that was passed but configured with required functionality.
     */
    private fun configure(mapper: ObjectMapper): ObjectMapper {
        return mapper.apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            registerModule(KotlinModule())
            registerModule(Jdk8Module())
            registerModule(ParameterNamesModule())
            registerModule(JavaTimeModule())

            val module = SimpleModule().apply {
                addDeserializer(JsonArray::class.java, JsonArrayDeserializer())
                addDeserializer(JsonObject::class.java, JsonObjectDeserializer())
            }

            registerModule(module)
        }
    }

    private class JsonObjectDeserializer : StdDeserializer<JsonObject>(JsonObject::class.java) {
        override fun deserialize(p: JsonParser?, ctx: DeserializationContext?): JsonObject {
            return JsonObject(p!!.readValueAsTree<JsonNode>().toString())
        }
    }

    private class JsonArrayDeserializer : StdDeserializer<JsonArray>(JsonArray::class.java) {
        override fun deserialize(p: JsonParser?, ctx: DeserializationContext?): JsonArray {
            return JsonArray(p!!.readValueAsTree<JsonNode>().toString())
        }
    }
}