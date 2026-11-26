package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationRateTest {
    @Test
    fun aNavigationReadsAFixEverySecondWhateverTheDistanceMoved() {
        assertEquals(1_000L, LocationRate.Navigation.intervalMilliseconds)
        assertEquals(0f, LocationRate.Navigation.distanceMeters)
    }

    /**
     * A prompt falls due 55 m before its maneuver and is said by a fix between 55 and 50 m out, so a walker's fixes
     * stand at most the prompt's 5 m of slack apart up to 9 km/h.
     */
    @Test
    fun aWalkerIsReadEveryTwoSecondsOnceTheDeviceMovedFiveMeters() {
        assertEquals(2_000L, LocationRate.Walking.intervalMilliseconds)
        assertEquals(5f, LocationRate.Walking.distanceMeters)
        assertEquals(5.0, 2.5 * LocationRate.Walking.intervalMilliseconds / 1_000.0)
    }

    @Test
    fun theRatesAreANavigationsAndAWalkers() {
        assertEquals(listOf(LocationRate.Navigation, LocationRate.Walking), LocationRate.entries)
    }
}
