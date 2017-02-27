package io.datawire.deployd

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.datawire.deployd.health.DeploydHealthCheck
import io.datawire.deployd.resource.WebhookResource
import io.datawire.deployd.resource.WorldsResource
import io.dropwizard.Application
import io.dropwizard.forms.MultiPartBundle
import io.dropwizard.jackson.Jackson
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment


class Deployd : Application<DeploydConfiguration>() {

    override fun initialize(bootstrap: Bootstrap<DeploydConfiguration>?) {
        bootstrap?.objectMapper?.registerModule(KotlinModule())
        bootstrap?.addBundle(MultiPartBundle())
        super.initialize(bootstrap)
    }

    override fun run(configuration: DeploydConfiguration, environment: Environment) {
        environment.healthChecks().register("service", DeploydHealthCheck());

        val yamlMapper = Jackson.newObjectMapper(YAMLFactory())
        yamlMapper.registerModule(KotlinModule())

        initializeDeployd(configuration)

        environment.jersey().apply {
            register(WorldsResource(configuration.workspace, environment.objectMapper))
            register(WebhookResource(configuration.workspace, yamlMapper))
        }
    }

    private fun initializeDeployd(configuration: DeploydConfiguration) {
        configuration.workspace.initialize()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Deployd().run(*args)
        }
    }
}