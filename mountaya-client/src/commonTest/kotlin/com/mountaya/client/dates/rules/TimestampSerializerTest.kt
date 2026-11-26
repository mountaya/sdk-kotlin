package com.mountaya.client

import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class TimestampSerializerTest {
    @Test
    fun aTimestampIsReadThroughTheAnchoredRule() {
        assertEquals(
            Instant.fromEpochSeconds(1_773_480_413),
            wireJson.decodeFromString(TimestampSerializer, "\"2026-03-14T10:26:53+01:00\""),
        )
    }

    @Test
    fun aValueThatFailsTheAnchorFailsTheDecode() {
        for (text in listOf("2026-03-14", "2026-03-14T09:26:53", "soon")) {
            assertFailsWith<SerializationException>(
                text,
            ) { wireJson.decodeFromString(TimestampSerializer, "\"$text\"") }
        }
    }

    @Test
    fun anInstantIsWrittenInUtcToTheMillisecond() {
        assertEquals(
            "\"2026-03-14T09:26:53.000Z\"",
            wireJson.encodeToString(TimestampSerializer, Instant.fromEpochSeconds(1_773_480_413)),
        )
        assertEquals(
            "\"2026-03-14T09:26:53.999Z\"",
            wireJson.encodeToString(TimestampSerializer, Instant.fromEpochSeconds(1_773_480_413, 999_999_999)),
        )
    }
}
