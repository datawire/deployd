package io.datawire.deployd

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.datawire.deployd.health.DeploydHealthCheck
import io.datawire.deployd.resource.WebhookResource
import io.dropwizard.Application
import io.dropwizard.forms.MultiPartBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import org.glassfish.jersey.media.multipart.MultiPartFeature


class Deployd : Application<DeploydConfiguration>() {

    override fun initialize(bootstrap: Bootstrap<DeploydConfiguration>?) {
        bootstrap?.objectMapper?.registerModule(KotlinModule())
        bootstrap?.addBundle(MultiPartBundle())
        super.initialize(bootstrap)
    }

    override fun run(configuration: DeploydConfiguration, environment: Environment) {
        environment.healthChecks().register("service", DeploydHealthCheck());

        environment.jersey().apply {
            register(WebhookResource(configuration.workspace))
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Deployd().run(*args)
        }
    }
}