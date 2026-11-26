package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LocationStandingTest {
    private val whenInUse = LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Full)

    @Test
    fun twoStandingsAreEqualWhereBothMembersAre() {
        assertEquals(whenInUse, LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Full))
        val same = LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Full)
        assertEquals(whenInUse.hashCode(), same.hashCode())
        assertNotEquals(whenInUse, LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Reduced))
        assertNotEquals(whenInUse, LocationStanding(LocationAuthorization.Always, LocationAccuracy.Full))
        assertEquals("LocationStanding(authorization=WhenInUse, accuracy=Full)", whenInUse.toString())
    }
}
