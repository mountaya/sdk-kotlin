package com.mountaya.geo.profile.contracts

import com.mountaya.geo.ProfileTotals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProfileTotalsTest {
    @Test
    fun twoTotalsAreEqualWhenEveryFigureIs() {
        val totals = ProfileTotals(7_117.5, 495.0, 278.0, 1_035.0, 1_480.0, 14.1)

        assertEquals(totals, ProfileTotals(7_117.5, 495.0, 278.0, 1_035.0, 1_480.0, 14.1))
        assertEquals(totals.hashCode(), ProfileTotals(7_117.5, 495.0, 278.0, 1_035.0, 1_480.0, 14.1).hashCode())
        assertNotEquals(totals, ProfileTotals(7_117.5, null, 278.0, 1_035.0, 1_480.0, 14.1))
        assertNotEquals(
            ProfileTotals(0.0, 0.0, null, null, null, null),
            ProfileTotals(0.0, -0.0, null, null, null, null),
        )
        assertEquals(
            "ProfileTotals(distance=0.0, ascent=null, descent=null, elevationMin=null, elevationMax=null, gradeMax=null)",
            ProfileTotals(0.0, null, null, null, null, null).toString(),
        )
    }
}
