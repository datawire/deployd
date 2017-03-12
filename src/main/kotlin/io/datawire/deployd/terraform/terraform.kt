package io.datawire.deployd.terraform

import com.fasterxml.jackson.annotation.JsonProperty
import io.datawire.deployd.world.AwsProvider
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


data class TerraformInOutMapping(@JsonProperty val inputs: Map<String, String>,
                                 @JsonProperty val outputs: Map<String, String>)

sealed class TerraformPlanResult(val statusCode: Int)

data class SucceededWithoutDifferences(val workspace: Path, val plan: Path) : TerraformPlanResult(0)
data class SucceededWithDifferences(val workspace: Path, val plan: Path)    : TerraformPlanResult(2)

object Failed : TerraformPlanResult(1)

data class RemoteParameters(val s3Bucket: String,
                            val s3Region: String) {

    constructor(provider: AwsProvider): this(provider.s3StateStore, provider.region)
}


fun terraformGenerate(workspace: Path, parameters: Map<String, String>) {

}

fun terraformSetup(workspace: Path, service: String, remote: RemoteParameters) {
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

fun terraformApply(planResult: SucceededWithDifferences) {
    val command = listOf("/home/plombardi/bin/terraform", "apply")
    val options = listOf("-no-color")

    val fullCommand = (command + options) + planResult.plan.toString()
    val (res, data) = execute(planResult.workspace, fullCommand)

    println(res)
    println(data)

    if (res != 0) {
        throw RuntimeException("""Failed `terraform apply` (result: $res)

Full command = '$fullCommand'
""")
    }
}

fun terraformPlan(workspace: Path, variablesFile: Path?, destroy: Boolean = false): TerraformPlanResult {
    val planPath = workspace.resolve("plan.out")
    val command = listOf("/home/plombardi/bin/terraform", "plan")
    val options = listOf("-no-color", "-detailed-exitcode", "-out=$planPath")

    if (destroy) {
        options + "-destroy"
    }

    val fullCommand = (command + options)
    val (res, data) = execute(workspace, fullCommand)

    println(res)
    println(data)

    return when(res) {
        0    -> SucceededWithoutDifferences(workspace, planPath)
        1    -> Failed
        2    -> SucceededWithDifferences(workspace, planPath)
        else -> throw IllegalStateException("""Unexpected `terraform plan` result (expect: [0, 1, 2], seen: $res)
Full command = '$fullCommand'
""")
    }
}

private fun execute(workingDirectory: Path, args: List<String>): Pair<Int, String?> {
    val pb = ProcessBuilder().apply {
        directory(workingDirectory.toFile())
        command(args)
        environment().put("AWS_DEFAULT_REGION", "us-east-1")
        redirectErrorStream(true)
    }

    val proc = pb.start()
    val output = proc.inputStream.bufferedReader().use { it.readText() }

    val timedOut = !proc.waitFor(10L, TimeUnit.MINUTES)
    return if (!timedOut) Pair(proc.exitValue(), output) else throw TimeoutException("Timed out on command: `${pb.command()}`")
}

fun main(vararg args: String) {
    terraformSetup(Paths.get("hack/scratch"),
            "foobar",
            RemoteParameters("foobar", "us-east-1"))

//    val (res, out) = execute(Paths.get("/home/plombardi/datawire/terraform-test"), listOf("terraform", "--help"))
//
//    println("Result = $res")
//    println("Output = $out")
}
