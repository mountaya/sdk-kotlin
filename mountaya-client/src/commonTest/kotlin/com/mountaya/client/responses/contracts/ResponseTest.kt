package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ResponseTest {
    @Test
    fun aResponseIsItsDataItsMetadataAndWhatItsAnswerSaid() {
        val created = Response("hut", 3, status = 201, requestId = "4bf9", location = "/v1/huts/h1", etag = "\"d7\"")

        assertEquals(Response("hut", 3), Response("hut", 3))
        assertNotEquals<Response<String, Int?>>(Response("hut", 3), Response("hut", null))
        assertNotEquals(Response("hut", 3), created)
        assertEquals(201, created.status)
        assertEquals("/v1/huts/h1", created.location)
        assertEquals("\"d7\"", created.etag)
        assertEquals("4bf9", created.requestId)
        assertEquals(200, Response("hut", null).status)
        assertNull(Response("hut", null).requestId)
        assertEquals("Response(status=200, data=hut, metadata=null)", Response("hut", null).toString())
    }

    @Test
    fun itsHeadersAreEveryOneTheAnswerSentNamedInLowercaseAndReadInAnyCase() {
        val response =
            Response(
                "hut",
                null,
                headers =
                    mapOf(
                        "Cache-Control" to "private",
                        "Vary" to "Origin",
                        "vary" to "X-API-Key",
                    ),
            )

        assertEquals(mapOf("cache-control" to "private", "vary" to "Origin, X-API-Key"), response.headers)
        assertEquals("private", response.header("CACHE-CONTROL"))
        assertNull(response.header("ETag"))
        assertEquals(emptyMap(), Response("hut", null).headers)
        assertEquals(
            response,
            Response(
                "hut",
                null,
                headers =
                    mapOf(
                        "cache-control" to "private",
                        "VARY" to "Origin, X-API-Key",
                    ),
            ),
        )
        assertNotEquals(response, Response("hut", null))
    }
}
