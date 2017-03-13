package io.datawire.md.fabric

import io.vertx.core.logging.LoggerFactory


class TfModuleFactory {

    private val logger = LoggerFactory.getLogger(TfModuleFactory::class.java)

    fun create(ctx: PlanningContext): TfModule {

        logger.debug("Planning Context => {}", ctx)

        val vars = ctx.moduleSpec.inputs.mapValues { (_, spec) ->
            val value = ctx.parameters[spec.source] ?: spec.default

            logger.debug("""Processing Terraform variable specification...
Type    = ${spec.type}
Source  = ${spec.source}
Default = ${spec.default}
""")

            // terraform only knows how to handle string, list and map types. all other logical types (e.g. "boolean")
            // are converted to string implicitly. Further the contents of lists and maps must be strings.
            val tfType = spec.type.toLowerCase()
            if (tfType !in setOf("string", "list", "map")) {
                throw IllegalArgumentException("Invalid terraform variable type for ${spec.source}... was: ${spec.type} not in [string,list,map]")
            }

            when {
                value is String   && tfType == "string"  -> value
                value is Number   && tfType == "string"  -> value.toString()
                value is Boolean  && tfType == "string"  -> value.toString()
                value is List<*>  && tfType == "list"    -> value.map(Any?::toString)
                value is Map<*,*> && tfType == "map"     -> value.map { (k, v) -> Pair(k.toString(), v.toString()) }
                else -> { throw RuntimeException("illegal type mapping ($value, $tfType, $spec") }
            }
        }

        return TfModule(ctx.moduleSpec.name, ctx.moduleSpec.source, TfVariables(vars))
    }
}
