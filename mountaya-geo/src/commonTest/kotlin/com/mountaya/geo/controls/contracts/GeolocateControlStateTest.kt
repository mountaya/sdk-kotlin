package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeolocateControlStateTest {
    @Test
    fun aStateShowsNothingUntilToldAndTwoAreEqualWhereEveryFlagIs() {
        assertEquals(GeolocateControlState(isBusy = false, isBlocked = false, isLive = false), GeolocateControlState())
        assertNotEquals(GeolocateControlState(), GeolocateControlState(isBusy = true))
        assertNotEquals(GeolocateControlState(isLive = true), GeolocateControlState(isBlocked = true))
    }
}
