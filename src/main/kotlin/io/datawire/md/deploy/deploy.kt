package io.datawire.md.deploy

import io.datawire.md.EmptyConfig
import io.datawire.deployd.persistence.Workspace
import io.datawire.deployd.service.ServiceSpec
import io.datawire.md.*
import io.datawire.md.deploy.terraform.*
import io.datawire.md.deploy.terraform.TfModule
import io.datawire.md.deploy.terraform.TfModuleSpec
import io.datawire.md.fabric.*
import io.datawire.md.service.ServicePersistence
import io.datawire.md.service.ServiceRef
import io.datawire.md.service.validate
import io.datawire.vertx.BaseVerticle
import io.datawire.vertx.VerticleDeployer
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.DeploymentOptions
import java.nio.file.Paths
import java.util.*


private const val UPSERT_SERVICE   = "deploy.service:upsert"
private const val UPDATE_TERRAFORM = "deploy.service:update-terraform"
private const val PLAN_TERRAFORM   = "deploy.service:plan-terraform"
private const val APPLY_TERRAFORM  = "deploy.service:apply-terraform"
private const val GENERATE_KUBE    = "deploy.service:deploy-kube"


class DeploymentVerticle : BaseVerticle<EmptyConfig>(EmptyConfig::class) {

    private val terraform = Terraformer()

    override fun start(startFuture: Future<Void>?) {
        registerEventBusCodec(Deployment::class)
        registerEventBusCodec(DeploymentContext::class)
        registerEventBusCodec(ServiceSpec::class)

        checkKubernetesConnectivity()

        startFuture?.complete()
        start()
    }

    override fun start() {
        vertx.eventBus().localConsumer<ServiceSpec>(UPSERT_SERVICE).handler(this::updateSpecification)
        vertx.eventBus().localConsumer<DeploymentContext>(UPDATE_TERRAFORM).handler(this::updateTerraform)
        vertx.eventBus().localConsumer<DeploymentContext>(PLAN_TERRAFORM).handler(this::planTerraform)
        vertx.eventBus().localConsumer<DeploymentContext>(APPLY_TERRAFORM).handler(this::applyTerraform)
    }

    private fun checkKubernetesConnectivity() {
        val ns = DefaultKubernetesClient().namespaces().withName("kube-system").get()
        logger.debug("Found 'kube-system' namespace... We can talk to Kubernetes!")
        ns ?: throw RuntimeException("Could not find main kubernetes namespace")
    }

    private fun writeTerraformTemplate(serviceName: String, template: TfTemplateOld) {
        val fsPath = "services/$serviceName/terraform.tf.json"
        Workspace.writeFile(vertx, fsPath, toBuffer(toJson(template)))
    }

    private fun loadServiceSpec(serviceName: String, deploymentId: String? = null): ServiceSpec? {
        val fsPath = "services/$serviceName/service${ deploymentId?.let {"-$it"} }.yaml"
        if (Workspace.contains(vertx, fsPath)) {
            val data = Workspace.readFile(vertx, fsPath)
            return fromJson(data)
        } else {
            return null
        }
    }

    private fun writeServiceSpec(service: ServiceSpec) {
        val fsPath = "services/${service.name}/service.yaml"
        Workspace.writeFile(vertx, fsPath, toBuffer(toYaml(service)))
    }

    private fun updateSpecification(msg: Message<ServiceSpec>) {
        val service = msg.body()

        val deployment = Deployment(
                id      = UUID.randomUUID().toString(),
                update  = UpdateType.SPECIFICATION_UPDATE,
                status  = DeploymentStatus.NOT_STARTED,
                service = ServiceRef(service.name, null))

        try {
            updateDeployment(deployment)
            validate(vertx, service)

            val fsRoot = "services/${service.name}"
            val fsPath = "$fsRoot/service-${deployment.id}.yaml"
            Workspace.createDirectories(vertx, fsRoot)
            Workspace.writeFile(vertx, fsPath, toBuffer(toYaml(service)))

            msg.reply(deployment)
            vertx.eventBus().send(UPDATE_TERRAFORM, DeploymentContext(deployment, service))
        } catch (any: Throwable) {
            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
            msg.fail(1, any.message)
        }
    }

    private fun planTerraform(msg: Message<DeploymentContext>) {
//        val deploymentCtx = msg.body()!!
//        val deployment = deploymentCtx.deployment
//        val fabricStore = FabricPersistence(vertx)
//        val fabric = fabricStore.getFabric()!!
//
//        try {
//            val path = Paths.get(Workspace.path(vertx)).resolve("services/${deploymentCtx.service.name}")
//
//
//            terraformSetup(path,
//                    deploymentCtx.service.name,
//                    RemoteParameters(fabric.terraform.stateBucket, "us-east-1"))
//
//            val res = terraformPlan(path, null, false)
//
//            println(res)
//
//            vertx.eventBus().send(APPLY_TERRAFORM, deploymentCtx)
//
//        } catch (any: Throwable) {
//            logger.error("ERROR", any)
//            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
//        }
    }

    private fun applyTerraform(msg: Message<DeploymentContext>) {
//        val deploymentCtx = msg.body()!!
//        val deployment = deploymentCtx.deployment
//        val fabricStore = FabricPersistence(vertx)
//        val fabric = fabricStore.getFabric()
//
//        try {
//            val path = Paths.get(Workspace.path(vertx)).resolve("services/${deploymentCtx.service.name}")
//
//            val plan = SucceededWithDifferences(path, path.resolve("plan.out"))
//            terraformApply(SucceededWithDifferences(path, path.resolve("plan.out")))
//            updateDeployment(deployment.copy(status = DeploymentStatus.SUCCEEDED))
//
//        } catch (any: Throwable) {
//            logger.error("ERROR", any)
//            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
//        }
    }

    private fun updateTerraform(msg: Message<DeploymentContext>) {
        val deployCtx  = msg.body()
        val deployment = deployCtx.deployment
        val newSpec    = deployCtx.service

        updateDeployment(deployment.copy(status = DeploymentStatus.IN_PROGRESS))
        try {
            val serviceStore = ServicePersistence(vertx)
            val fabricStore = FabricPersistence(vertx)

            val currentSpec = serviceStore.getServiceByName(deployment.service.name)
            if (currentSpec != newSpec) {

                val fabric = fabricStore.getFabric()
                val requirements = mutableListOf<Pair<TfModule, List<TfOutput>>>()
                newSpec.requires.fold(requirements) { acc, req ->
                    val mod = fabricStore.getModuleById(req.id) as TfModuleSpec // TODO: BAD

                    val params = req.params + fabric!!.parameters + mapOf("__fabric_name__" to fabric.name, "__service_name__" to deployment.service.name)
                    val tfGenCtx = TfGenerateModuleContext(req.alias, mod, params)

                    requirements += terraform.generateModuleAndOutputs(tfGenCtx)
                    requirements
                }

                val template = terraform.generateTemplate(fabric!!.amazon.toTerraformProvider(), requirements)
                val json = toJson(template)
                serviceStore.putFile(deployment.service, "terraform.tf.json", toBuffer(json))

                vertx.eventBus().send(PLAN_TERRAFORM, deployCtx)
            } else {
                updateDeployment(deployment.copy(status = DeploymentStatus.SUCCEEDED))
            }
        } catch (any: Throwable) {
            logger.error("ERROR", any)
            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
        }



//        val ctx = msg.body()
//        val deployment = ctx.deployment
//        val latestSpec = ctx.service
//        updateDeployment(deployment.copy(status = DeploymentStatus.IN_PROGRESS))
//
//        try {
//            val currentSpec = loadServiceSpec(ctx.service.name)
//            if (latestSpec.requires != currentSpec?.requires) {
//                val tfModFactory = TfModuleFactory()
//                val mods = latestSpec.requires.map {
//                    val fabricStore = FabricPersistence(vertx)
//                    val fabric = fabricStore.getFabric()
//                    val module = fabricStore.getModuleById(it.id)
//
//
//                    val planningCtx = PlanningContext(
//                            deployId     = deployment.id,
//                            moduleSpec   = module as TerraformModuleSpec,
//                            parameters   = Parameters(
//                                    (it.params + fabric.parameters) +
//                                            mapOf("__service_name__" to deployment.service.name,
//                                                    "__fabric_name__" to fabric.name))
//                    )
//
//                    tfModFactory.create(planningCtx)
//                }
//
//                val template = TfTemplate(mods.associateBy { it.name })
//                writeTerraformTemplate(latestSpec.name, template)
//
//                vertx.eventBus().send(PLAN_TERRAFORM, ctx)
//            } else {
//                // replace the current spec with the old so everything else is up to date
//                writeServiceSpec(ctx.service)
//
//                // update status to complete
//                updateDeployment(deployment.copy(status = DeploymentStatus.SUCCEEDED))
//            }
//        } catch (any: Throwable) {
//            logger.error("ERROR", any)
//            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
//        }
    }

    private fun updateDeployment(deployment: Deployment) {
        val deployments = vertx.sharedData().getLocalMap<String, JsonObject>("md.deployments")
        deployments.put(deployment.id, toJsonObject(deployment))
    }

    companion object : VerticleDeployer() {
        override fun deploy(vertx: Vertx, config: JsonObject, result: (AsyncResult<String>) -> Unit) {
            val options = DeploymentOptions(
                    config = JsonObject(),
                    worker = true)

            vertx.deployVerticle(DeploymentVerticle::class.qualifiedName, options, result)
        }
    }
}







































