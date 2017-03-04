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

package io.datawire.vertx.config

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.databind.node.TreeTraversingParser
import kotlin.reflect.KClass


class ConfigFactory<out T: Any>(private val mapper: ObjectMapper, private val configClass: Class<T>) {

  constructor(mapper: ObjectMapper, configClass: KClass<T>) : this(mapper, configClass.java)

  fun create(source: ConfigSource, node: JsonNode): T {
    return try {
      val config = mapper.readValue<T>(TreeTraversingParser(node), configClass);
      config
    } catch (ex: UnrecognizedPropertyException) {
      val issueLocation = ConfigParseException.SourceLocation(ex.location)
      throw ConfigParseException(source, issueLocation, "Unrecognized field or property", ex.message)
    } catch (ex: InvalidFormatException) {
      val sourceJavaType = ex.value.javaClass.simpleName
      val targetJavaType = ex.targetType.simpleName
      val issueLocation = ConfigParseException.SourceLocation(ex.location)
      throw ConfigParseException(source,
                                 issueLocation,
                                 "Incorrect data type of JSON value",
                                 "is of type: $sourceJavaType, expected: $targetJavaType",
                                 ex.path, ex)
    } catch (ex: JsonMappingException) {
      val issueLocation = ConfigParseException.SourceLocation(ex.location)
      throw ConfigParseException(source, issueLocation, "Failed to parse configuration", ex.message)
    }
  }

  fun create(source: ConfigSource) : T {
    return source.open().use { stream ->
      mapper.readTree(stream)?.let { node -> create(source, node) } ?:
          throw ConfigParseException(source, ConfigParseException.SourceLocation(0, 0), "Config is empty", "")
    }
  }
}