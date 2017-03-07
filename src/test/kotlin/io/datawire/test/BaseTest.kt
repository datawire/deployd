package io.datawire.test

import io.datawire.vertx.json.ObjectMappers
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.LocalMap
import io.vertx.ext.unit.junit.Timeout
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.ServerSocket
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


@RunWith(VertxUnitRunner::class)
abstract class BaseTest {

    @get:Rule
    val timeoutRule: Timeout = Timeout.seconds(60)

    companion object {
        @JvmStatic
        @BeforeClass
        fun beforeSetup() {
            // Enforce that these tests run in UTC. MCP servers operate in UTC.
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
        }
    }

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    protected val objectMapper = ObjectMappers.prettyMapper

    // Database configuration parameters for when needed.
    private val postgresUrl = URI.create(System.getProperty("mcp.postgres.url", "postgresql://127.0.0.1:5432/mcp"))
    private val postgresUsername = System.getProperty("mcp.postgres.username", "mcp")
    private val postgresPassword = System.getProperty("mcp.postgres.password", "mcp")

//    protected val databaseConfig = DatabaseConfig(host     = postgresUrl.host,
//            port     = postgresUrl.port,
//            username = postgresUsername,
//            password = postgresPassword,
//            database = postgresUrl.path.replaceFirst("/", ""),
//            connectionPoolName = "mcp.test")

    // -------------------------------------------------------------------------------------------------------------------
    // Useful container types
    // -------------------------------------------------------------------------------------------------------------------

    data class MCPUser(val organization: String,
                       val user: String,
                       val email: String,
                       val roles: JsonArray = JsonArray(),
                       val permissions: JsonArray = JsonArray()) {

        private fun generateJWTPayload() =
                JsonObject().apply {
                    put("sub", user)
                    put("email", email)
                    put("aud", organization)
                    put("iat", Instant.now().epochSecond)
                    put("exp", Instant.now().plusSeconds(3600).epochSecond)
                    put("roles", roles)
                    put("permissions", permissions)
                }

//        fun generateJWT(vertx: Vertx, algorithm: String = "none"): String {
//            val allowedAlgorithms = setOf("none", "HS256")
//            if (algorithm !in allowedAlgorithms) {
//                throw IllegalStateException("Algorithm is not supported (allowed: $allowedAlgorithms, given: $algorithm)")
//            }
//
//            val keyStoreConfig = mapOf(
//                    "path" to "keystore-HS256.jceks",
//                    "type" to "jceks",
//                    "password" to "secret")
//
//            val authProviderConfig = JsonObject(mapOf("keyStore" to keyStoreConfig))
//            val authProvider = JWTAuth.create(vertx, authProviderConfig)
//
//            val payload = generateJWTPayload()
//            val token = authProvider.generateToken(payload, JWTOptions().setSubject(user).setAlgorithm(algorithm))
//
//            // bug in the library that none tokens do not have the third segment generated so we generate some garbage.
//            return if (algorithm == "none") token + "none" else token
//        }
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Network utilities
    // -------------------------------------------------------------------------------------------------------------------

    /**
     * Acquires a random available port. It's not perfect for avoiding port collisions but is "good enough" for most
     * serious uses.
     *
     * @return the available port to use.
     */
    protected fun reserveListenPort(): Int {
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port
    }

    // -------------------------------------------------------------------------------------------------------------------
    // HTTP utilities
    // -------------------------------------------------------------------------------------------------------------------

    protected fun configureCommon(req: HttpClientRequest, token: String) {
        req.apply {
            putHeader(HttpHeaders.ACCEPT, "application/json")
            putHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
        }
    }

    protected fun urlEncode(text: String, encoding: Charset = Charsets.UTF_8): String {
        return URLEncoder.encode(text, encoding.name())
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Vert.x SharedData utilities
    // -------------------------------------------------------------------------------------------------------------------

    protected fun <A, B> localMap(vertx: Vertx, name: String): LocalMap<A, B?> {
        return vertx.sharedData().getLocalMap<A, B>(name)
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Vert.x EventBus utilities
    // -------------------------------------------------------------------------------------------------------------------

    protected fun publish(vertx: Vertx, to: String, message: Any, options: DeliveryOptions = DeliveryOptions()) {
        vertx.eventBus().publish(to, message, options)
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Resources utilities
    // -------------------------------------------------------------------------------------------------------------------

    fun resourceStream(name: String): InputStream {
        return this.javaClass.getResourceAsStream("/$name")
    }

    fun resource(name: String, charset: Charset = Charsets.UTF_8): String {
        return resourceStream(name).bufferedReader(charset).readText()
    }

    protected fun resourceJsonArray(name: String) = JsonArray(resource(name))

    protected fun resourceJsonObject(name: String) = JsonObject(resource(name))

    // -------------------------------------------------------------------------------------------------------------------
    // Database / JDBC utilities
    // -------------------------------------------------------------------------------------------------------------------

//    protected fun createJDBCClient(vertx: Vertx): JDBCClient {
//        val json = JsonObject().apply {
//            put("url", "jdbc:$postgresUrl")
//            put("user", postgresUsername)
//            put("password", postgresPassword)
//            put("castUUID", true)
//        }
//
//        return JDBCClient.createShared(vertx, json, "mcp.test")
//    }
//
//    protected fun truncate(jdbc: JDBCClient, tables: Set<String>, resultHandler: Handler<AsyncResult<UpdateResult>>) {
//        truncate(jdbc, tables, { resultHandler.handle(it) })
//    }
//
//    protected fun truncate(jdbc: JDBCClient, tables: Set<String>, resultHandler: (AsyncResult<UpdateResult>) -> Unit) {
//
//        /*
//         | TODO: Might be preferable?
//         |
//         | SELECT 'TRUNCATE ' || table_name || ';'
//         |   FROM information_schema.tables
//         |  WHERE table_schema='public'
//         |    AND table_type='BASE TABLE';
//        */
//
//        jdbc.getConnection { getConn ->
//            when {
//                getConn.succeeded() -> {
//                    val conn = getConn.result()
//                    conn.update("TRUNCATE ${tables.joinToString(",")} CASCADE;") { update -> resultHandler(update) }
//                    conn.close()
//                }
//                getConn.failed() -> {
//                    logger.error("Connect to DB failed (url: $postgresUrl)", getConn.cause())
//                }
//            }
//        }
//    }

    // -------------------------------------------------------------------------------------------------------------------
    // JSON utilities
    // -------------------------------------------------------------------------------------------------------------------

    /**
     * Deserialize a string containing JSON to specific type [T].
     *
     * @param text the [String] to deserialize.
     */
    protected inline fun <reified T: Any> fromJson(text: String): T {
        return objectMapper.readValue<T>(text, T::class.java)
    }

    /**
     * Deserialize a [JsonObject] to a specific type [T].
     *
     * @param json the [JsonObject] to deserialize.
     */
    protected inline fun <reified T: Any> fromJson(json: JsonObject): T = fromJson(json.encodePrettily())

    /**
     * Deserialize a [JsonArray] to a specific type [T].
     *
     * @param json the [JsonArray] to deserialize.
     */
    protected inline fun <reified T: Any> fromJson(json: JsonArray): T = fromJson(json.encodePrettily())

    /**
     * Serialize a type into JSON.
     *
     * @param item the instance of <T> to serialize.
     */
    protected fun <T: Any> toJson(item: T): String = objectMapper.writeValueAsString(item)

    // -------------------------------------------------------------------------------------------------------------------
    // Vertx utilities
    // -------------------------------------------------------------------------------------------------------------------

    /**
     * A hook method for subclasses to provide custom [VertxOptions] before the vert.x server is started.
     */
    protected open fun provideVertxOptions(clustered: Boolean): VertxOptions {
        return VertxOptions().setClustered(clustered)
    }

    /**
     * Create the [Vertx] instance that will be used by the test. When constructing a clustered [Vertx] this method will
     * block for upto a minute in order to allow the [Vertx] instance to initialize.
     */
    protected open fun createVertx(clustered: Boolean): Vertx {
        val vertxOptions = provideVertxOptions(clustered)

        if (clustered && (clustered != vertxOptions.isClustered)) {
            logger.warn("Class has property 'clustered' = true, but 'VertxOptions#isClustered' = false."
                    + " Check overridden implementation of #provideVertxOptions()?")
        }

        return if (clustered) {
            val latch = CountDownLatch(1)
            val vertx = AtomicReference<Vertx?>()
            Vertx.clusteredVertx(vertxOptions) { clustering ->
                when {
                    clustering.succeeded() -> {
                        vertx.set(clustering.result())
                        latch.countDown()
                    }
                    else -> throw RuntimeException("Acquire clustered vertx failed")
                }
            }

            try {
                latch.await(1L, TimeUnit.MINUTES)
                return vertx.get()!!
            } catch (interrupt: InterruptedException) { throw RuntimeException(interrupt) }
        } else {
            Vertx.vertx(vertxOptions)
        }
    }
}