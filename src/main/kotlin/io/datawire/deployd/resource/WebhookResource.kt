package io.datawire.deployd.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.datawire.deployd.api.DeploydMetadata
import io.datawire.deployd.api.Service
import io.datawire.deployd.api.Workspace
import io.datawire.deployd.prototype.ServiceManager
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.glassfish.jersey.media.multipart.FormDataParam
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType


@Path("/integrations")
class WebhookResource @Inject constructor(private val workspace: Workspace,
                                          private val objectMapper: ObjectMapper) {

    @Path("/develop")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun developmentWebhook(@FormDataParam("file") payload: InputStream,
                           @FormDataParam("file") disposition: FormDataContentDisposition) {

        val outputFile = Files.createTempFile(workspace.path, null, ".tar.gz")

        val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
        val extractDir = Files.createTempDirectory(workspace.path, disposition.name)
        archiver.extract(outputFile.toFile(), extractDir.toFile())

        val metadata = DeploydMetadata.load(objectMapper, extractDir.resolve("deployd.yaml"))
        val knownWorlds = Files.newDirectoryStream(workspace.worldsWorkspace).map { it.fileName.toString().replace(".json", "") }

        // throw an error if the service specifies non-existent worlds
        if (metadata.worlds.intersect(knownWorlds) != metadata.worlds.toSet()) {
            // TODO: proper exception and HTTP response
            throw RuntimeException("Unknown world(s) requested for modification (deployd.yaml: ${metadata.worlds}, tracked: $knownWorlds)")
        }

        val services = metadata.worlds.map { Service(it, metadata.service.name) }

        // housekeeping - services are isolated by world. "world1.hello" is different from "world2.hello".
        services.forEach {
            Files.createDirectories(workspace.servicesWorkspace.resolve("${it.world}_${it.name}"))
        }

        // Prototype Algorithm
        // -------------------
        //
        // 1. lookup the AWS provider region and credentials from the given world and ensure they're written into a
        //    file "aws.tf[.json]". (alternatively we may want to just use ENV vars for the credentials)
        //
        // 2. for the given requirements (metadata.requires) generate a Terraform module per that maps to the
        //    appropriate named thing in the Terraform repository.
        //
        //    For example if given:
        //
        //    requires:
        //      - postgresql96
        //
        //    Then generate a Terraform module that has a "source = ${repo}//postgresql96"
        //
        // 3. Run terraform plan and then terraform apply. We'll do this as an unsafe step right now so ignore the
        //    result of -detailed-exitcode from Terraform until we have an appropriate mechanism for accept/reject the
        //    proposed changes.
        //
        // 4. Lookup the variables that need to be mapped from Terraform -> Container and generate a Map that can fed
        //    to Kubernetes as a Secret.
        //
        // 5. Generate the appropriate Deployment and Service Kubernetes stuff along with the Environment variables
        //    to expose from Kubernetes and then inject them into the definition.
        //
        services.forEach {
            val world = null
        }
    }
}