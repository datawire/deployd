package io.datawire.deployd.terraform

import java.nio.file.Path


class TerraformPlan(val executable: Path) {

  fun run(path: Path, destroy: Boolean = false): TerraformPlanResult {

    val command = arrayOf(executable.toString(), "plan")
    val options = arrayOf("-no-color")

    if (destroy) {
      options + "-destroy" + "-out=${path.resolve("plan.out")}"
    }

    val fullCommand = (command + options + path.toString()).toList()

    val pb = ProcessBuilder()
    val terraform = pb.command(fullCommand).inheritIO().start()
    val res = terraform.waitFor()

    return when(res) {
      0    -> SucceededWithoutDifferences(path.resolve("plan.out"))
      1    -> Failed
      2    -> SucceededWithDifferences(path.resolve("plan.out"))
      else -> throw IllegalStateException("""Unexpected `terraform plan` result (expect: [0, 1, 2], seen: $res)

Full command = '$fullCommand'
""")
    }
  }
}