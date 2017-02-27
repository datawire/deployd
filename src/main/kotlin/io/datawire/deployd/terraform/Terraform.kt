package io.datawire.deployd.terraform

import java.nio.file.Path


class Terraform(private val executable: Path) {

    fun apply(planningResult: TerraformPlanResult) {
        when (planningResult) {
            is SucceededWithDifferences -> apply(planningResult.plan)
        }
    }

    private fun apply(plan: Path) {
        val terraform = executable.toAbsolutePath().toString()
        val res = execute(terraform, "apply", "-no-color", plan.toAbsolutePath().toString())
    }

    fun plan(workspace: Path): TerraformPlanResult {
        val terraform = executable.toAbsolutePath().toString()
        val res = execute(terraform, "plan", "-no-color", "-detailed-exitcode", "-out=plan.out")

        return when(res) {
            0    -> SucceededWithoutDifferences(workspace.resolve("plan.out"))
            1    -> Failed
            2    -> SucceededWithDifferences(workspace.resolve("plan.out"))
            else -> throw IllegalStateException("Unexpected `terraform plan` result (expect: [0, 1, 2], seen: $res)")
        }
    }

    private fun execute(vararg args: String): Int {
        val pb = ProcessBuilder()
        val cmd = pb.command(*args).inheritIO().start()
        return cmd.waitFor()
    }
}

//fun main(args: Array<String>) {
//    val pb = ProcessBuilder()
//    println(pb.environment())
//    val tf = pb.command("/home/plombardi/bin/terraform", "plan", "-detailed-exitcode", "-out=plan.out").inheritIO().start()
//    val exitCode = tf.waitFor()
//    println("exitCode = " + exitCode)
//}