package com.mountaya.elements

import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ReadoutPoint
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ReadoutZoneOfTest {
    private fun feature(vararg properties: Pair<String, JsonPrimitive>) =
        QueriedFeature(properties.toMap(), GeoJsonPoint(listOf(6.86, 45.83)), "layer")

    /** A zone reads its name, description, level and window, the times JSON numbers truncated to the second. */
    @Test
    fun aZoneReadsItsWordsAndItsWindowInWholeSeconds() {
        val zone =
            feature(
                "name" to JsonPrimitive("Réserve"),
                "description" to JsonPrimitive("Bivouac interdit"),
                "level" to JsonPrimitive("prohibited"),
                "start_at" to JsonPrimitive(1_700_000_000.9),
                "end_at" to JsonPrimitive(1_800_000_000),
            )

        assertEquals(
            ReadoutPoint.Zone(
                name = "Réserve",
                description = "Bivouac interdit",
                level = RestSchema.ZoneLevel("prohibited"),
                start = Instant.fromEpochSeconds(1_700_000_000),
                end = Instant.fromEpochSeconds(1_800_000_000),
            ),
            readoutZoneOf(zone),
        )
    }

    /** A time written as a string, digits or not, and one stated as null or not at all, is none. */
    @Test
    fun aZoneTimeWrittenAsAStringIsNone() {
        val zone =
            feature(
                "start_at" to JsonPrimitive("1700000000"),
                "end_at" to JsonPrimitive("2026-01-01T00:00:00Z"),
            )
        val stated = readoutZoneOf(zone)
        val unstated = readoutZoneOf(feature("start_at" to JsonNull))

        assertEquals(null, stated.start)
        assertEquals(null, stated.end)
        assertEquals(null, unstated.start)
        assertEquals(ReadoutPoint.Zone("", "", RestSchema.ZoneLevel(""), null, null), unstated)
    }
}
