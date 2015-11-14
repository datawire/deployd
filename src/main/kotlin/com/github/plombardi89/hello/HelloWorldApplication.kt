package com.github.plombardi89.hello

import io.dropwizard.Application
import io.dropwizard.setup.Environment


class HelloWorldApplication : Application<HelloWorldApplicationConfig>() {

  companion object {
    @JvmStatic fun main(args: Array<String>) {
      HelloWorldApplication().run(*args)
    }
  }

  override fun run(config: HelloWorldApplicationConfig, environment: Environment?) {

  }
}