package com.mountaya.client

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class WireJsonTest {
    @Serializable
    private class Probe(
        val kept: String,
        val missing: String?,
        val left: String? = null,
    )

    @Test
    fun aKeyThisBuildDoesNotKnowIsIgnored() {
        assertEquals("a", wireJson.decodeFromString(Probe.serializer(), """{"kept":"a","missing":null,"new":1}""").kept)
    }

    @Test
    fun aDefaultIsLeftOutAndANullHeldIsWritten() {
        assertEquals("""{"kept":"a","missing":null}""", wireJson.encodeToString(Probe.serializer(), Probe("a", null)))
    }
}
