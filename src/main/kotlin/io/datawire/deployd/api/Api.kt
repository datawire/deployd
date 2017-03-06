package io.datawire.deployd.api

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.datawire.vertx.json.ObjectMappers
import io.vertx.ext.web.Router


interface Api {

    /**
     * Register additional routes with the provided router.
     *
     * @param router the router to configure with additional routes.
     */
    fun configure(router: Router)

    fun <T: Any> result(item: T): String =
            ObjectMappers.prettyMapper.writeValueAsString(item)

    fun <T: Any> collectionResult(name: String, items: Collection<T>): String {
        val res = ObjectMappers.prettyMapper.writeValueAsString(Results(name, items))
        println(res)
        return res
    }
}

@JsonSerialize(using = ResultsSerializer::class)
data class Results<out T>(val name: String, val items: Collection<T>)

class ResultsSerializer : StdSerializer<Results<*>>(Results::class.java) {
    override fun serialize(value: Results<*>, gen: JsonGenerator, provider: SerializerProvider) {
        with(gen) {
            writeStartObject()
            writeArrayFieldStart(value.name)
            for (it in value.items) {
                writeObject(it)
            }
            writeEndArray()
            writeEndObject()
        }
    }
}