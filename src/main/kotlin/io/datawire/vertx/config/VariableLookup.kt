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

import org.apache.commons.lang3.text.StrLookup
import java.util.regex.Pattern


class VariableLookup(private val strict: Boolean) : StrLookup<String>() {

  private val variablePattern = Pattern.compile("^(\\w+)\\s+`(.*)`$")

  private val lookups = mapOf(
      "env" to EnvironmentVariableLookupHandler(),
      "file" to FileLookupHandler(),
      "prop" to PropertyLookupHandler())

  override fun lookup(key: String): String? {
    val matcher = variablePattern.matcher(key)
    return if (matcher.matches()) {
      val type = matcher.group(1)
      val source = matcher.group(2)

      val result = lookups[type.toLowerCase()]?.let { it.lookup(source) }

      if (result == null && strict) {
        throw UndefinedVariableException(type, source)
      }

      result
    } else {
      throw InvalidVariableFormatException(key)
    }
  }
}