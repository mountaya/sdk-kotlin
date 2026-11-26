package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class WaypointSummaryWordsTest {
    private val open: (String) -> String = { "open $it" }
    private val cutoff: (String) -> String = { "cut-off $it" }
    private val stop: (String) -> String = { "stop $it" }

    @Test
    fun theClausesAreTheCallersWords() {
        val words = WaypointSummaryWords("Services", open, cutoff, stop)

        assertEquals("open 8:00 to 18:00", words.openingHours("8:00 to 18:00"))
        assertEquals("WaypointSummaryWords(amenities=Services)", words.toString())
    }

    @Test
    fun twoAreEqualWhenTheyHoldTheSameWordsAndClauses() {
        assertEquals(
            WaypointSummaryWords("Services", open, cutoff, stop),
            WaypointSummaryWords("Services", open, cutoff, stop),
        )
        assertEquals(
            WaypointSummaryWords("Services", open, cutoff, stop).hashCode(),
            WaypointSummaryWords("Services", open, cutoff, stop).hashCode(),
        )
        assertNotEquals(
            WaypointSummaryWords("Services", open, cutoff, stop),
            WaypointSummaryWords("Services", open, cutoff, open),
        )
    }
}
