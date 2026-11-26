package com.mountaya.geo

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccuracyBoundsTest {
    private fun assertNear(
        expected: List<Double>,
        actual: List<Double>,
    ) {
        assertTrue(
            actual.size == expected.size && actual.indices.all { abs(actual[it] - expected[it]) <= 1e-9 },
            "$actual, where $expected is the rule's",
        )
    }

    @Test
    fun theBoxReachesTheRadiusOverTheCircumferenceEitherSideWiderInLongitude() {
        assertNear(
            listOf(-73.976086214142, 40.772701684716, -73.973713785858, 40.774498315284),
            accuracyBounds(listOf(-73.9749, 40.7736), 100.0),
        )
        assertNear(
            listOf(-0.008983152841, -0.008983152841, 0.008983152841, 0.008983152841),
            accuracyBounds(listOf(0.0, 0.0), 1000.0),
        )
        assertNear(
            listOf(25.706217853745, 70.975508423579, 25.733782146255, 70.984491576421),
            accuracyBounds(listOf(25.72, 70.98), 500.0),
        )
        assertNear(
            listOf(-70.671532853324, -33.467966305682, -70.628467146676, -33.432033694318),
            accuracyBounds(listOf(-70.65, -33.45), 2000.0),
        )
    }

    @Test
    fun aFixByTheAntimeridianKeepsItsEastEdgePastItAndNoAccuracyIsThePosition() {
        assertNear(
            listOf(179.998563103051, -16.500898315284, 180.000436896949, -16.499101684716),
            accuracyBounds(listOf(179.9995, -16.5), 100.0),
        )
        assertEquals(listOf(6.86, 45.83, 6.86, 45.83), accuracyBounds(listOf(6.86, 45.83), 0.0))
    }

    @Test
    fun nothingIsRefusedAndAFixIsFramedAtZoom16AtMost() {
        val short = accuracyBounds(listOf(6.86), 100.0)
        assertTrue(short[1].isNaN() && short[3].isNaN(), "$short")
        val unknown = accuracyBounds(listOf(6.86, 45.83), Double.NaN)
        assertTrue(unknown.all { it.isNaN() }, "$unknown")
        assertEquals(16.0, LOCATE_MAX_ZOOM)
    }
}
