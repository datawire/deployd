package io.datawire.vertx


import io.datawire.vertx.json.ObjectMappers
import io.datawire.vertx.config.ConfigFactory
import io.datawire.vertx.config.StringConfigSource
import io.datawire.vertx.config.SubstitutingConfigSource
import io.datawire.vertx.config.VariableLookup
import io.datawire.vertx.json.JacksonMessageCodec
import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import org.apache.commons.lang3.text.StrSubstitutor
import kotlin.reflect.KClass


abstract class BaseVerticle<out T : Config>(private val configClass: Class<T>) : Verticle {

    private companion object {
        init {
            ObjectMappers.configure(Json.mapper)
            ObjectMappers.configure(Json.prettyMapper)
        }
    }

    constructor(configClass: KClass<T>) : this(configClass.java)

    private lateinit var config: T
    private lateinit var vertx: Vertx
    private lateinit var context: Context

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * The configuration source that was initially passed to Vertx during deployment.
     */
    fun originalConfig(): JsonObject = context.config()

    /**
     * The bound configuration source after variable substitution is completed and mapping to a known configuration
     * class is completed.
     */
    fun config() = config

    override fun init(vertx: Vertx?, context: Context?) {
        this.vertx   = vertx!!
        this.context = context!!

        val configJson = StringConfigSource(this.context.config().encodePrettily())
        val substitutor = StrSubstitutor(VariableLookup(true))
        val configFactory = ConfigFactory(Json.prettyMapper, configClass)
        this.config = configFactory.create(SubstitutingConfigSource(configJson, substitutor))
    }

    fun <T: Any> registerEventBusCodec(clazz: KClass<T>) {
        vertx.eventBus().registerDefaultCodec(clazz.java, JacksonMessageCodec(clazz))
    }

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>?) {
        start()
        startFuture!!.complete()
    }

    @Throws(Exception::class)
    override fun stop(stopFuture: Future<Void>?) {
        stop()
        stopFuture!!.complete()
    }

    @Throws(Exception::class)
    open fun start() { }

    @Throws(Exception::class)
    open fun stop() { }

    override fun getVertx() = vertx

    fun deploymentID(): String = context.deploymentID()
}