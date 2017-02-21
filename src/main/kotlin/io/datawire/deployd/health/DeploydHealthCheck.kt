package io.datawire.deployd.health

import com.codahale.metrics.health.HealthCheck


class DeploydHealthCheck : HealthCheck() {

    override fun check(): Result = Result.healthy()
}