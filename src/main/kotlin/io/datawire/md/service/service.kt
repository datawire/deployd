package io.datawire.md.service

import io.datawire.deployd.service.ServiceSpec
import io.vertx.core.Vertx
import io.vertx.core.shareddata.SharedData


/**
 * Validate a service specification.
 */
fun validate(vertx: Vertx, service: ServiceSpec) {
    val errors = mutableListOf<String>()

    for (requirement in service.requires) {
        if (checkModuleIsRegistered(vertx.sharedData(), requirement.id)) { errors + "missing requirement: ${requirement.id}" }
    }

    if (errors.isNotEmpty()) {
        throw IllegalStateException("validation error!")
    }
}


private fun checkModuleIsRegistered(vertx: SharedData, moduleId: String): Boolean {
    val modules = vertx.getLocalMap<String, Boolean>("md.modules")
    return modules.get(moduleId) != null
}
