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

import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.databind.JsonMappingException


class ConfigParseException(source: ConfigSource,
                           location: SourceLocation?,
                           @Suppress("UNUSED_PARAMETER") summary: String,
                           detail: String?,
                           issues: List<JsonMappingException.Reference> = emptyList(),
                           cause: Throwable? = null) : RuntimeException(formatMessage(source, location, detail, issues), cause) {

  data class SourceLocation(val lineNumber: Int, val columnNumber: Int) {
    constructor(location: JsonLocation) : this(location.lineNr, location.columnNr)
  }

  companion object {

    fun formatMessage(@Suppress("UNUSED_PARAMETER") source: ConfigSource,
                      location: SourceLocation?,
                      detail: String?,
                      issues: List<JsonMappingException.Reference>): String {

      var result = ""
      if (issues.isNotEmpty()) {
        result += " at: " + buildPathToIssue(issues)
      } else if (location != null) {
        result += " at line: " + location.lineNumber
        result += " at column: " + location.columnNumber
      }

      detail?.let { result += ";" + System.lineSeparator() + it }

      return result
    }

    private fun buildPathToIssue(issues: List<JsonMappingException.Reference>): String {
      return issues.map { ref -> ref.fieldName ?: "[${ref.index}]" }.joinToString(separator = ".")
    }
  }
}