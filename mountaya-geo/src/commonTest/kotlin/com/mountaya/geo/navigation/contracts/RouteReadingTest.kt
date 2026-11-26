package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class RouteReadingTest {
    @Test
    fun aReadingHoldsTheProgressAndTheDistanceItWasGiven() {
        val progress =
            NavigationProgress(
                along = 120.0,
                step = 0,
                next = 1,
                toManeuver = NavigationRemainder(880.0, null),
                toArrival = NavigationRemainder(1_880.0, null),
            )
        val reading = RouteReading(progress, 12.5)

        assertSame(progress, reading.progress)
        assertEquals(12.5, reading.meters)
    }
}
