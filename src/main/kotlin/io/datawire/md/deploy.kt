package io.datawire.md

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.EmptyConfig
import io.datawire.deployd.persistence.Workspace
import io.datawire.deployd.service.Service
import io.datawire.deployd.terraform.*
import io.datawire.md.fabric.*
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


enum class DeploymentStatus {
    @JsonProperty("not-started")
    NOT_STARTED,

    @JsonProperty("in-progress")
    IN_PROGRESS,

    @JsonProperty("succeeded")
    SUCCEEDED,

    @JsonProperty("failed")
    FAILED
}

enum class UpdateType {
    @JsonProperty("specification")
    SPECIFICATION_UPDATE,

    @JsonProperty("implementation")
    IMPLEMENTATION_UPDATE
}


data class Deployment(@JsonProperty val id      : String,
                      @JsonProperty val update  : UpdateType,
                      @JsonProperty val status  : DeploymentStatus,
                      @JsonProperty val service : ServiceRef)

data class DeploymentContext(@JsonProperty val deployment: Deployment, @JsonProperty val service: Service)


class DeploymentVerticle : BaseVerticle<EmptyConfig>(EmptyConfig::class) {

    override fun start(startFuture: Future<Void>?) {
        registerEventBusCodec(Deployment::class)
        registerEventBusCodec(DeploymentContext::class)
        registerEventBusCodec(Service::class)

        checkKubernetesConnectivity()

        startFuture?.complete()
        start()
    }

    override fun start() {
        vertx.eventBus().localConsumer<Service>(UPSERT_SERVICE).handler(this::updateSpecification)
        vertx.eventBus().localConsumer<DeploymentContext>(UPDATE_TERRAFORM).handler(this::updateTerraform)
        vertx.eventBus().localConsumer<DeploymentContext>(PLAN_TERRAFORM).handler(this::planTerraform)
        vertx.eventBus().localConsumer<DeploymentContext>(APPLY_TERRAFORM).handler(this::applyTerraform)
    }

    private fun checkKubernetesConnectivity() {
        val ns = DefaultKubernetesClient().namespaces().withName("kube-system").get()
        logger.debug("Found 'kube-system' namespace... We can talk to Kubernetes!")
        ns ?: throw RuntimeException("Could not find main kubernetes namespace")
    }

    private fun writeTerraformTemplate(serviceName: String, template: TfTemplate) {
        val fsPath = "services/$serviceName/terraform.tf.json"
        Workspace.writeFile(vertx, fsPath, toBuffer(toJson(template)))
    }

    private fun loadServiceSpec(serviceName: String, deploymentId: String? = null): Service? {
        val fsPath = "services/$serviceName/service${ deploymentId?.let {"-$it"} }.yaml"
        if (Workspace.contains(vertx, fsPath)) {
            val data = Workspace.readFile(vertx, fsPath)
            return fromJson(data)
        } else {
            return null
        }
    }

    private fun writeServiceSpec(service: Service) {
        val fsPath = "services/${service.name}/service.yaml"
        Workspace.writeFile(vertx, fsPath, toBuffer(toYaml(service)))
    }

    private fun updateSpecification(msg: Message<Service>) {
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
        val deploymentCtx = msg.body()!!
        val deployment = deploymentCtx.deployment
        val fabric = readFabric(vertx)!!

        try {
            val path = Paths.get(Workspace.path(vertx)).resolve("services/${deploymentCtx.service.name}")

            terraformSetup(path,
                    deploymentCtx.service.name,
                    RemoteParameters(fabric.terraform.stateBucket, "us-east-1"))

            val res = terraformPlan(path, null, false)

            println(res)

            vertx.eventBus().send(APPLY_TERRAFORM, deploymentCtx)

        } catch (any: Throwable) {
            logger.error("ERROR", any)
            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
        }
    }

    private fun applyTerraform(msg: Message<DeploymentContext>) {
        val deploymentCtx = msg.body()!!
        val deployment = deploymentCtx.deployment
        val fabric = readFabric(vertx)!!

        try {
            val path = Paths.get(Workspace.path(vertx)).resolve("services/${deploymentCtx.service.name}")

            val plan = SucceededWithDifferences(path, path.resolve("plan.out"))
            terraformApply(SucceededWithDifferences(path, path.resolve("plan.out")))
            updateDeployment(deployment.copy(status = DeploymentStatus.SUCCEEDED))

        } catch (any: Throwable) {
            logger.error("ERROR", any)
            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
        }
    }

    private fun updateTerraform(msg: Message<DeploymentContext>) {
        val ctx = msg.body()
        val deployment = ctx.deployment
        val latestSpec = ctx.service
        updateDeployment(deployment.copy(status = DeploymentStatus.IN_PROGRESS))

        try {
            val currentSpec = loadServiceSpec(ctx.service.name)
            if (latestSpec.requires != currentSpec?.requires) {
                val tfModFactory = TfModuleFactory()
                val mods = latestSpec.requires.map {
                    val fabric = readFabric(vertx)!!
                    val module = lookupModule(vertx, it.id)!! // TODO: no bueno

                    val planningCtx = PlanningContext(
                            deployId     = deployment.id,
                            moduleSpec   = module,
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







































