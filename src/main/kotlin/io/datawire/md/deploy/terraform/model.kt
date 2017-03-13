package io.datawire.md.deploy.terraform

import io.datawire.md.deploy.Deployment
import io.datawire.md.service.ServiceRef


data class GenerateTemplateContext(
        val service: ServiceRef,
        val deployment: Deployment)