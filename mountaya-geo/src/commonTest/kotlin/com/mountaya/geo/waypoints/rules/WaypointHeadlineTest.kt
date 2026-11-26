package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WaypointHeadlineTest {
    private val words = WaypointSummaryWords("Services", { "open $it" }, { "cut-off $it" }, { "stop $it" })
    private val english = UnitPreferences("en")

    @Test
    fun aDescriptionWinsTheHeadlineAndTheSummaryStandsInForNone() {
        assertEquals(
            "The old refuge.",
            waypointHeadline(
                restWaypoint(amenities = listOf("hut"), description = " The old refuge.\n"),
                words,
                english,
            ),
        )
        assertEquals(
            "Services",
            waypointHeadline(restWaypoint(amenities = listOf("hut"), description = "  "), words, english),
        )
        assertEquals("Services", waypointHeadline(restWaypoint(amenities = listOf("hut")), words, english))
        assertNull(waypointHeadline(restWaypoint(), words, english))
    }
}
