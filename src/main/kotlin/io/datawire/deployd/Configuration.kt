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

package io.datawire.deployd

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.datawire.vertx.Config

data class ServerConfig(
        @JsonProperty("port") val port: Int = 5000,
        @JsonProperty("host") val host: String = "0.0.0.0") : Config

data class ApiConfig(
        @JsonProperty
        @JsonUnwrapped
        val server: ServerConfig = ServerConfig(port = 52689, host = "0.0.0.0")) : Config


data class DeploydConfig(@JsonProperty("api") val api: ApiConfig) : Config
