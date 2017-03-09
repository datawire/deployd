package io.datawire.deployd.terraform

import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.nio.file.Paths


sealed class TerraformPlanResult(val statusCode: Int)

data class SucceededWithoutDifferences(val workspace: Path, val plan: Path) : TerraformPlanResult(0)
data class SucceededWithDifferences(val workspace: Path, val plan: Path)    : TerraformPlanResult(2)

object Failed : TerraformPlanResult(1)

//fun terraformPrepare(workspace: Path) {
//
//}
//
//fun terraformOutput(workspace: Path): Map<String, String> {
//    val command = listOf("terraform", "output")
//    val options = listOf("-json")
//
//    val fullCommand = (command + options)
//    val res = execute(planResult.workspace, fullCommand)
//}

fun terraformApply(planResult: SucceededWithDifferences) {
    val command = listOf("terraform", "apply")
    val options = listOf("-no-color")

    val fullCommand = (command + options) + planResult.plan.toString()
    val (res, data) = execute(planResult.workspace, fullCommand)

    if (res != 0) {
        throw RuntimeException("""Failed `terraform apply` (result: $res)

Full command = '$fullCommand'
""")
    }
}

fun terraformPlan(workspace: Path, variablesFile: Path, destroy: Boolean = false): TerraformPlanResult {
    val planPath = workspace.resolve(workspace)
    val command = listOf("terraform", "plan")
    val options = listOf("-no-color", "-var-file=$variablesFile", "-detailed-exitcode", "-out=$planPath")

    if (destroy) {
        options + "-destroy"
    }

    val fullCommand = (command + options)
    val (res, data) = execute(workspace, fullCommand)

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
        redirectErrorStream(true)
    }

    val proc = pb.start()
    val output = proc.inputStream.bufferedReader().use { it.readText() }

    val timedOut = !proc.waitFor(10L, TimeUnit.MINUTES)
    return if (!timedOut) Pair(proc.exitValue(), output) else throw TimeoutException("Timed out on command: `${pb.command()}`")
}

//fun main(vararg args: String) {
//    val (res, out) = execute(Paths.get("/home/plombardi"), listOf("terraform", "--help"))
//
//    println("Result = $res")
//    println("Output = $out")
//}
