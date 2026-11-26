package com.mountaya.client

import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NullableSerializerTest {
    @Test
    fun nullIsReadAndWrittenBesideTheValue() {
        val serializer = nullableSerializer(Int.serializer())

        assertNull(wireJson.decodeFromString(serializer, "null"))
        assertEquals(3, wireJson.decodeFromString(serializer, "3"))
        assertEquals("null", wireJson.encodeToString(serializer, null))
    }
}
