package io.datawire.deployd.p2

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.datawire.deployd.health.DeploydHealthCheck
import io.dropwizard.Application
import io.dropwizard.forms.MultiPartBundle
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import java.util.concurrent.ConcurrentHashMap

typealias WorldRepo   = InMemoryObjectRepository<World>
typealias ServiceRepo = InMemoryObjectRepository<Service>

fun main(args: Array<String>) {
  Deployd().run(*args)
}

class Deployd : Application<DeploydConfig>() {

  override fun initialize(bootstrap: Bootstrap<DeploydConfig>?) {

    bootstrap!!.apply {
      objectMapper.registerKotlinModule()
      addBundle(MultiPartBundle())
    }

    super.initialize(bootstrap)
  }

  override fun run(configuration: DeploydConfig, environment: Environment) {
    environment.healthChecks().register("service", DeploydHealthCheck());

    val worlds   = WorldRepo()
    val services = ServiceRepo()

    environment.jersey().apply {
      register(DeploymentsResource::class.java)
      register(ServicesResource::class.java)
      register(WorldsResource::class.java)
    }
  }
}

class InMemoryObjectRepository<T : Identifiable>() {

  private val backingMap = ConcurrentHashMap<String, T>()

  operator fun contains(key: String) = key in backingMap

  operator fun get(key: String) = getByName(key)

  fun add(candidate: T) {
    backingMap.putIfAbsent(candidate.id, candidate)
  }

  fun remove(name: String) {
    backingMap.remove(name)
  }

  fun getByName(name: String) = backingMap[name]

  fun getAll(): Collection<T> = backingMap.values
}
