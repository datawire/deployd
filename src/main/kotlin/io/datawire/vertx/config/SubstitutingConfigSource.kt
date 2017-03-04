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

import org.apache.commons.lang3.text.StrSubstitutor
import java.io.*


class SubstitutingConfigSource(private val delegate: ConfigSource,
                               private val substitutor: StrSubstitutor) : ConfigSource by delegate {

  @Throws(IOException::class)
  override fun open(): InputStream {
    return delegate.open().bufferedReader().use {
      val substituted = substitutor.replace(it.readText())
      substituted.byteInputStream(Charsets.UTF_8)
    }
  }
}