package io.datawire.deployd

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.datawire.deployd.health.DeploydHealthCheck
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment


class Deployd : Application<DeploydConfiguration>() {

    override fun initialize(bootstrap: Bootstrap<DeploydConfiguration>?) {
        bootstrap?.objectMapper?.registerModule(KotlinModule())
        super.initialize(bootstrap)
    }

    override fun run(configuration: DeploydConfiguration, environment: Environment) {
        environment.healthChecks().register("service", DeploydHealthCheck());
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Deployd().run(*args)
        }
    }
}