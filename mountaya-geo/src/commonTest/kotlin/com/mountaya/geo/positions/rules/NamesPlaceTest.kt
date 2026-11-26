package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NamesPlaceTest {
    @Test
    fun aLongitudeAndALatitudeNameAPlaceWithOrWithoutAnAltitude() {
        assertTrue(namesPlace(listOf(6.8652, 45.9237)))
        assertTrue(namesPlace(listOf(6.8652, 45.9237, 1035.0)))
        assertTrue(namesPlace(listOf(6.8652, 45.9237, Double.NaN)))
        // A value past the range is still a place: a rule reads it as an angle, or as written.
        assertTrue(namesPlace(listOf(190.0, 95.0)))
    }

    @Test
    fun aPositionWrittenShortNamesNoPlace() {
        assertFalse(namesPlace(emptyList()))
        assertFalse(namesPlace(listOf(6.88)))
    }

    @Test
    fun aLongitudeOrALatitudeThatIsNotFiniteNamesNoPlace() {
        assertFalse(namesPlace(listOf(Double.NaN, 45.0)))
        assertFalse(namesPlace(listOf(6.0, Double.NaN)))
        assertFalse(namesPlace(listOf(Double.POSITIVE_INFINITY, 45.0)))
        assertFalse(namesPlace(listOf(6.0, Double.NEGATIVE_INFINITY)))
    }
}
