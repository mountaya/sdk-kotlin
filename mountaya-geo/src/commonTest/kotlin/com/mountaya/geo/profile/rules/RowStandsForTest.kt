package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RowStandsForTest {
    @Test
    fun aRowAnswersToItsOwnIdAndToAMemberItStandsForNeverToNone() {
        val group =
            ItinerarySegmentRow(
                key = "group:g1",
                id = "group:g1",
                memberIds = listOf("s2", "s3"),
                name = "",
                index = 1,
                from = 0.0,
                to = 1.0,
                metrics = ItinerarySegmentRowMetrics(),
            )

        assertTrue(rowStandsFor(group, "group:g1"))
        assertTrue(rowStandsFor(group, "s3"))
        assertFalse(rowStandsFor(group, "s9"))
        assertFalse(rowStandsFor(group, null))
        assertFalse(rowStandsFor(group, "S3"))
    }
}
