package io.datawire.deployd.terraform

import java.nio.file.Path


sealed class TerraformPlanResult(val statusCode: Int)

data class SucceededWithoutDifferences(val plan: Path) : TerraformPlanResult(0)
data class SucceededWithDifferences(val plan: Path)    : TerraformPlanResult(1)

object Failed : TerraformPlanResult(1)