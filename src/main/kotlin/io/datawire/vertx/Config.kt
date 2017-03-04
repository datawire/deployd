package io.datawire.vertx

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Marker interface for classes that are for server configuration.
 *
 * @author plombardi@datawire.io
 */

@JsonIgnoreProperties(ignoreUnknown = true)
interface Config