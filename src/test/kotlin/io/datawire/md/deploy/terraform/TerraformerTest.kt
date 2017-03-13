package io.datawire.md.deploy.terraform

import io.datawire.md.fromYaml
import io.datawire.test.BaseTestUsingVertx
import org.junit.Test


class TerraformerTest : BaseTestUsingVertx() {

    private val simpleModuleSpec = """---
name: basic
version: 1
source: github.com/foo/bar
inputs:
    - target: fabric
      type: string
      source: __fabric_name__

    - target: service
      type: string
      source: __service_name__

    - target: aStringVar
      type: string
      source: bar

    - target: aStringVarWithDefaultedValue
      type: string
      source: NOT_PRESENT
      default: aBoringString

    - target: aList
      type: list
      source: aListOfStrings

outputs:
    - name: DB_NAME
      target: db_name

    - name: DB_USER
      target: db_user
"""

    @Test
    fun generateSimpleModule() {
        val modSpec = fromYaml<TfModuleSpec>(simpleModuleSpec)
        val injectedParams = mapOf<String, Any>(
                "__fabric_name__"  to "test-fabric",
                "__service_name__" to "test-service",
                "bar" to "PinkFloyd",
                "aListOfStrings" to listOf("apple", "pear")
        )

        val ctx = TfGenerateModuleContext("simple", modSpec, injectedParams)

        val tf = Terraformer()
        val res = tf.generateModuleAndOutputs(ctx)
        val template = tf.generateTemplate(listOf(res))
        println(toJson(template))
    }
}