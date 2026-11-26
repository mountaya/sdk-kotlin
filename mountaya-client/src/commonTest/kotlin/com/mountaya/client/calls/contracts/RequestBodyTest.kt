package com.mountaya.client

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RequestBodyTest {
    @Test
    fun aJsonBodyIsTheTextSent() {
        assertEquals(RequestBody.Json("""{"name":"Hut"}"""), RequestBody.Json("""{"name":"Hut"}"""))
        assertNotEquals<RequestBody>(RequestBody.Json("1"), RequestBody.Empty)
        assertNotEquals(RequestBody.Json("1"), RequestBody.Json("1.0"))
        assertEquals("Json(1)", RequestBody.Json("1").toString())
    }

    @Test
    fun aJsonValueACallerHandsIsWrittenByTheClientsJson() {
        val value = JsonObject(mapOf("name" to JsonPrimitive("Hut"), "beds" to JsonPrimitive(12), "note" to JsonNull))

        assertEquals(RequestBody.Json("""{"name":"Hut","beds":12,"note":null}"""), RequestBody.Json(value))
        assertEquals("""{"name":"Hut","beds":12,"note":null}""", RequestBody.Json(value).text)
    }

    @Test
    fun aFormBodyIsItsPartsInOrder() {
        val first = RequestBody.Multipart(listOf(MultipartPart.Text("a", "1"), MultipartPart.Text("b", "2")))
        val second = RequestBody.Multipart(listOf(MultipartPart.Text("b", "2"), MultipartPart.Text("a", "1")))

        assertNotEquals(first, second)
        assertEquals(first, RequestBody.Multipart(listOf(MultipartPart.Text("a", "1"), MultipartPart.Text("b", "2"))))
    }
}
