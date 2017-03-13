package io.datawire.md.deploy.terraform

import io.datawire.md.core.execute
import io.vertx.core.logging.LoggerFactory


class Terraformer {

    private val logger = LoggerFactory.getLogger(Terraformer::class.java)

    private fun logTerraformOutput(code: Int, message: String) {
        logger.debug("""--- Terraform Result (exitcode: {}) ---
{}
""", code, message)
    }

    fun configure(ctx: TfPlanContext) {
        val configCommand = listOf("/home/plombardi/bin/terraform", "remote", "config")
        val options = listOf(
                "-no-color",
                "-backend=s3",
                "-backend-config=bucket=${ctx.remoteState.bucket}",
                "-backend-config=key=${ctx.remoteState.name}.tfstate",
                "-backend-config=region=${ctx.remoteState.region}")

        val (configRes, configOutput) = execute((configCommand + options), ctx.modulePath, emptyMap())
        logTerraformOutput(configRes, configOutput)

        val updateCommand = listOf("/home/plombardi/bin/terraform", "get", "-update=true", "-no-color")
        val (updateRes, updateOutput) = execute(updateCommand, ctx.modulePath, emptyMap())
        logTerraformOutput(updateRes, updateOutput)
    }

    fun apply(ctx: TfApplyContext) {
        val command = listOf("/home/plombardi/bin/terraform", "apply")
        val options = listOf("-no-color", "-input=false", ctx.planPath)

        val fullCommand = (command + options)
        val (res, output) = execute(fullCommand, cwd = ctx.modulePath, env = emptyMap())
        logTerraformOutput(res, output)
    }

    fun plan(ctx: TfPlanContext): String {
        val command = listOf("/home/plombardi/bin/terraform", "plan")
        val options = listOf("-no-color", "-input=false", "-detailed-exitcode", "-out=${ctx.modulePath}")

        if (ctx.destroy) {
            options + "-destroy"
        }

        val fullCommand = (command + options)
        val (res, output) = execute(fullCommand, cwd = ctx.modulePath, env = emptyMap())
        logTerraformOutput(res, output)

        return "${ctx.modulePath}/plan.out"
    }

    fun generateModuleAndOutputs(ctx: TfGenerateModuleContext): Pair<TfModule, List<TfOutput>> {
        val vars = ctx.moduleSpec.inputs.associateBy { it.target }.mapValues { (_, tfVarSpec) ->
            val value = ctx.mappableParameters[tfVarSpec.source] ?: tfVarSpec.default

            if (value != null) {
                logger.debug("Map injected parameter to Terraform variable succeeded (source: {}, target: {})", tfVarSpec.source, tfVarSpec.target)
            } else {
                logger.error("Map injected parameter to Terraform variable failed because the injected parameter value was null (source: {}, target: {})", tfVarSpec.source, tfVarSpec.target)
                throw IllegalStateException("SEE LOG")
            }

            when {
                value is String   && tfVarSpec.type == TfVariableType.STRING -> value
                value is Number   && tfVarSpec.type == TfVariableType.STRING -> value.toString()
                value is Boolean  && tfVarSpec.type == TfVariableType.STRING -> value.toString()
                value is List<*>  && tfVarSpec.type == TfVariableType.LIST   -> value.map(Any?::toString)
                value is Map<*,*> && tfVarSpec.type == TfVariableType.MAP    -> value.map { (k, v) -> Pair(k.toString(), v.toString()) }
                else -> {
                    throw IllegalArgumentException(
                            "Type casting failed (terraform-type: ${tfVarSpec.type}, jvm-type: ${value::class.simpleName})")
                }
            }
        }

        val outputs = ctx.moduleSpec.outputs.map { (name, target) ->
            TfOutput("${ctx.moduleName}_$name", "\${module.${ctx.moduleName}.$target}")
        }

        val module = TfModule(ctx.moduleName, ctx.moduleSpec.source, vars)
        return Pair(module, outputs)
    }

    fun generateTemplate(provider: TfProvider, modulesAndOutputs: List<Pair<TfModule, List<TfOutput>>>): TfTemplate {
        val template = TfTemplate(providers = mapOf(provider.name to provider))
        return modulesAndOutputs.fold(template) { template, (module, outputs) ->
            template.copy(
                    modules = template.modules + Pair(module.name, module),
                    outputs = template.outputs + outputs.associateBy { it.name }
            ) }
    }
}