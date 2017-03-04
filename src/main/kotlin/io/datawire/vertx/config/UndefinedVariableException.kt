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


class UndefinedVariableException(type: String, source: String) : RuntimeException(formatMessage(type, source)) {

  companion object {
    private fun formatMessage(type: String, source: String): String {
      return "Variable substitution could not be performed with the given expression (expression: \${$type `$source`})"
    }
  }
}