package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationStandingOfTest {
    @Test
    fun noLocationPermissionReadsAsNeverAsked() {
        assertEquals(
            LocationStanding(LocationAuthorization.NotDetermined, LocationAccuracy.Full),
            locationStandingOf(fine = false, coarse = false, background = false),
        )
    }

    @Test
    fun aBackgroundPermissionIsAlwaysAndEitherOtherAloneWhileInUse() {
        assertEquals(
            LocationStanding(LocationAuthorization.Always, LocationAccuracy.Full),
            locationStandingOf(fine = true, coarse = true, background = true),
        )
        assertEquals(
            LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Full),
            locationStandingOf(fine = true, coarse = false, background = false),
        )
    }

    /** The approximate permission without the fine one places a traveller kilometres away. */
    @Test
    fun theApproximatePermissionAloneIsReducedAccuracy() {
        assertEquals(
            LocationStanding(LocationAuthorization.WhenInUse, LocationAccuracy.Reduced),
            locationStandingOf(fine = false, coarse = true, background = false),
        )
        assertEquals(
            LocationStanding(LocationAuthorization.Always, LocationAccuracy.Reduced),
            locationStandingOf(fine = false, coarse = true, background = true),
        )
    }
}
