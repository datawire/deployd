package io.datawire.md.deploy.terraform

import io.datawire.deployd.terraform.RemoteParameters
import io.datawire.md.fabric.FabricPersistence
import io.datawire.md.service.ServicePersistence
import io.datawire.md.toBuffer
import io.datawire.md.toJson
import io.vertx.core.Vertx


class Terraform(private val vertx: Vertx) {

    private val fabricStore  = FabricPersistence(vertx)
    private val serviceStore = ServicePersistence(vertx)

    fun generateTemplate(ctx: GenerateTemplateContext) {
        val file = "service.tf.json"



        // diffing logic will need to come another day


        val spec = serviceStore.getServiceByName(ctx.service.name)





//        val ctx = msg.body()
//        val deployment = ctx.deployment
//        val latestSpec = ctx.service
        updateDeployment(deployment.copy(status = DeploymentStatus.IN_PROGRESS))

        try {
            val currentSpec = loadServiceSpec(ctx.service.name)
            if (latestSpec.requires != currentSpec?.requires) {
                val tfModFactory = TfModuleFactory()
                val mods = latestSpec.requires.map {
                    val fabricStore = FabricPersistence(vertx)
                    val fabric = fabricStore.getFabric()
                    val module = fabricStore.getModuleById(it.id)


                    val planningCtx = PlanningContext(
                            deployId     = deployment.id,
                            moduleSpec   = module as TerraformModuleSpec,
                            parameters   = Parameters(
                                    (it.params + fabric.parameters) +
                                            mapOf("__service_name__" to deployment.service.name,
                                                    "__fabric_name__" to fabric.name))
                    )

                    tfModFactory.create(planningCtx)
                }

                val template = TfTemplate(mods.associateBy { it.name })
                writeTerraformTemplate(latestSpec.name, template)

                vertx.eventBus().send(PLAN_TERRAFORM, ctx)
            } else {
                // replace the current spec with the old so everything else is up to date
                writeServiceSpec(ctx.service)

                // update status to complete
                updateDeployment(deployment.copy(status = DeploymentStatus.SUCCEEDED))
            }
        } catch (any: Throwable) {
            logger.error("ERROR", any)
            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
        }
        serviceStore.putFile(ctx.service, file, toBuffer(toJson(1)))
    }
}


fun terraformPlan() {

}

fun terraformSetup(service: String, remote: RemoteParameters) {
    // TODO: remove the hard path reference
    val command = listOf("/home/plombardi/bin/terraform", "remote", "config")
    val options = listOf(
            "-backend=s3",
            "-backend-config=bucket=${remote.s3Bucket}",
            "-backend-config=key=$service.tfstate",
            "-backend-config=region=${remote.s3Region}")

    val fullCommand = (command + options)
    val (res, data) = execute(workspace, fullCommand)

    println(res)
    println(data)

    if (res != 0) {
        throw RuntimeException("""Failed `terraform remote config` (result: $res)

Full command = '$fullCommand'
""")
    }
}