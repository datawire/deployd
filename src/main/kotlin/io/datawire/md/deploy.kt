package io.datawire.md

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.EmptyConfig
import io.datawire.deployd.fabric.lookupModule
import io.datawire.deployd.persistence.Workspace
import io.datawire.deployd.service.Service
import io.datawire.deployd.service.TerraformRequirement
import io.datawire.md.fabric.Parameters
import io.datawire.md.fabric.PlanningContext
import io.datawire.md.fabric.TfModuleFactory
import io.datawire.md.service.ServiceRef
import io.datawire.md.service.validate
import io.datawire.vertx.BaseVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.util.*


private const val UPSERT_SERVICE   = "deploy.service:upsert"
private const val UPDATE_TERRAFORM = "deploy.service:update-terraform"


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

        startFuture?.complete()
        start()
    }

    override fun start() {
        vertx.eventBus().localConsumer<Service>(UPSERT_SERVICE).handler(this::updateSpecification)
        vertx.eventBus().localConsumer<DeploymentContext>(UPDATE_TERRAFORM).handler(this::updateTerraform)
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

            val fsPath = "services/${service.name}/service-${deployment.id}.yaml"
            Workspace.createDirectories(vertx, fsPath)
            Workspace.writeFile(vertx, fsPath, toBuffer(toYaml(service)))

            msg.reply(deployment)
            vertx.eventBus().send(UPDATE_TERRAFORM, DeploymentContext(deployment, service))
        } catch (any: Throwable) {
            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
            msg.fail(1, any.message)
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
                for (r in latestSpec.requires) {
                    val module = lookupModule(vertx, r.id)!! // TODO: no bueno

                    val planningCtx = PlanningContext(
                            deployId     = deployment.id,
                            moduleSpec   = module,
                            parameters   = Parameters(r.params)
                    )

                    val tfMod = tfModFactory.create(planningCtx)
                }

                updateDeployment(deployment.copy(status = DeploymentStatus.SUCCEEDED))
            } else {
                // replace the current spec with the old so everything else is up to date
                writeServiceSpec(ctx.service)

                // update status to complete
                updateDeployment(deployment.copy(status = DeploymentStatus.SUCCEEDED))
            }
        } catch (any: Throwable) {
            updateDeployment(deployment.copy(status = DeploymentStatus.FAILED))
        }
    }

    private fun updateDeployment(deployment: Deployment) {
        val deployments = vertx.sharedData().getLocalMap<String, JsonObject>("md.deployments")
        deployments.put(deployment.id, toJsonObject(deployment))
    }
}







































