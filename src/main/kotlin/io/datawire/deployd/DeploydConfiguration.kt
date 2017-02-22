package io.datawire.deployd

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.api.Workspace
import io.dropwizard.Configuration


class DeploydConfiguration(@JsonProperty val workspace: Workspace) : Configuration() {

}