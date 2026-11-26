package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ItinerarySegmentRowMetricsTest {
    private fun stated(difficulty: RestSchema.TrailDifficultyType? = RestSchema.TrailDifficultyType("T3")) =
        ItinerarySegmentRowMetrics(sectionDistance = 4_200.0, duration = 3_600.0, difficulty = difficulty)

    @Test
    fun everyFigureIsNullUnlessGiven() {
        val metrics = ItinerarySegmentRowMetrics()

        assertNull(metrics.distance)
        assertNull(metrics.sectionDistance)
        assertNull(metrics.ascent)
        assertNull(metrics.descent)
        assertNull(metrics.sectionAscent)
        assertNull(metrics.sectionDescent)
        assertNull(metrics.altitudeMin)
        assertNull(metrics.altitudeMax)
        assertNull(metrics.duration)
        assertNull(metrics.difficulty)
    }

    @Test
    fun twoAreEqualWhereEveryFigureIsTheSameOrAbsentOnBoth() {
        assertEquals(stated(), stated())
        assertEquals(stated().hashCode(), stated().hashCode())
        assertNotEquals(stated(difficulty = null), stated())
        assertNotEquals(ItinerarySegmentRowMetrics(ascent = -0.0), ItinerarySegmentRowMetrics(ascent = 0.0))
        assertNotEquals(ItinerarySegmentRowMetrics(), ItinerarySegmentRowMetrics(ascent = 0.0))
    }
}
