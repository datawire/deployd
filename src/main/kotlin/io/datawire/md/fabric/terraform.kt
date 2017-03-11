package io.datawire.md.fabric


import io.datawire.vertx.json.ObjectMappers


class TfModuleFactory {

    fun create(ctx: PlanningContext): TfModule {
        val vars = ctx.moduleSpec.inputs.mapValues { (_, spec) ->
            val value = ctx.parameters[spec.source]

            // terraform only knows how to handle string, list and map types. all other logical types (e.g. "boolean")
            // are converted to string implicitly. Further the contents of lists and maps must be strings.
            val tfType = spec.type.toLowerCase()
            if (tfType !in setOf("string", "list", "map")) {
                throw IllegalArgumentException("Invalid terraform variable type")
            }

            when {
                value is String   && tfType == "string"  -> value
                value is Number   && tfType == "string"  -> value.toString()
                value is Boolean  && tfType == "string"  -> value.toString()
                value is List<*>  && tfType == "list"    -> value.map(Any?::toString)
                value is Map<*,*> && tfType == "map"     -> value.map { (k, v) -> Pair(k.toString(), v.toString()) }
                else -> { throw RuntimeException("illegal type mapping") }
            }
        }

        return TfModule(ctx.moduleSpec.source, TfVariables(vars))
    }
}


fun main(args: Array<String>) {
    val mapper = ObjectMappers.prettyMapper
    val tfModules = TfModuleFactory()

    val modSpec = TfModuleSpec("foo", 1, "github", mapOf("world_name" to TfVariableSpec("string", "fabric.name")), mapOf())

    val ctx = PlanningContext("deploy-001", modSpec, Parameters(mapOf(
            "fabric.name"  to "development",
            "service.name" to "hello-goodbye"
    )))

    val module = tfModules.create(ctx)
    val template = TfTemplate(mapOf("foo" to module))

    println("---- Module ----")
    println(mapper.writeValueAsString(template))
}