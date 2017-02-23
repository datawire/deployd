package io.datawire.deployd.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.datawire.deployd.api.DeploydMetadata
import io.datawire.deployd.api.Workspace
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
        val bytes = Files.copy(payload, outputFile, StandardCopyOption.REPLACE_EXISTING)

        val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
        val extractDir = Files.createTempDirectory(workspace.path, disposition.name)
        archiver.extract(outputFile.toFile(), extractDir.toFile())

        val metadata = DeploydMetadata.load(objectMapper, extractDir.resolve("deployd.yaml"))
        println(metadata)
    }
}