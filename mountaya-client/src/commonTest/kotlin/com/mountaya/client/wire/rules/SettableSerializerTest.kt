package com.mountaya.client

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SettableSerializerTest {
    private val serializer = SettableSerializer(String.serializer().nullable)

    @Test
    fun aSetValueIsWrittenAsItselfNullIncluded() {
        assertEquals("\"Alps\"", wireJson.encodeToString(serializer, Settable.Present("Alps")))
        assertEquals("null", wireJson.encodeToString(serializer, Settable.Present(null)))
    }

    @Test
    fun anyValueThatArrivedWasSetNullIncluded() {
        assertEquals(Settable.Present("Alps"), wireJson.decodeFromString(serializer, "\"Alps\""))
        assertEquals(Settable.Present(null), wireJson.decodeFromString(serializer, "null"))
    }

    @Test
    fun nothingSetIsNeverWrittenAsNull() {
        assertFailsWith<SerializationException> { Json.encodeToString(serializer, Settable.Absent) }
    }
}
