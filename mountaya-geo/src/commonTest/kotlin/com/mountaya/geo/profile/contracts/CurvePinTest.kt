package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CurvePinTest {
    private val place = ProfilePlace(ProfileWaypoint.AtDistance("col", 250.0), name = "Col")

    @Test
    fun twoPinsAreEqualWhereEveryMemberIsTheSame() {
        val pin = CurvePin("col#0", place, 250.0, 50.0, 51.0, 1_250.0)

        assertEquals(CurvePin("col#0", place, 250.0, 50.0, 51.0, 1_250.0), pin)
        assertEquals(CurvePin("col#0", place, 250.0, 50.0, 51.0, 1_250.0).hashCode(), pin.hashCode())
        assertNotEquals(CurvePin("col#1", place, 250.0, 50.0, 51.0, 1_250.0), pin)
    }
}
