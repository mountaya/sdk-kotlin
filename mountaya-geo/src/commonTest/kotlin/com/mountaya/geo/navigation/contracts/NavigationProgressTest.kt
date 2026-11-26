package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NavigationProgressTest {
    private fun progress(
        along: Double,
        next: Int? = 1,
    ) = NavigationProgress(along, 0, next, NavigationRemainder(100.0, 60.0), NavigationRemainder(300.0, 120.0))

    @Test
    fun twoReadingsAreEqualWhereEveryMemberIs() {
        assertEquals(progress(200.0), progress(200.0))
        assertEquals(progress(200.0).hashCode(), progress(200.0).hashCode())
        assertEquals(progress(Double.NaN), progress(Double.NaN))
        assertNotEquals(progress(0.0), progress(-0.0))
        assertNotEquals(progress(200.0), progress(200.0, next = null))
        assertEquals(
            "NavigationProgress(along=200.0, step=0, next=1, " +
                "toManeuver=NavigationRemainder(distance=100.0, duration=60.0), " +
                "toArrival=NavigationRemainder(distance=300.0, duration=120.0))",
            progress(200.0).toString(),
        )
    }
}
