package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GeolocationFixTest {
    @Test
    fun twoFixesAreEqualWhereTheyHoldTheSameEachNumberTheSame() {
        assertEquals<GeolocationFix>(
            GeolocationFix.Located(listOf(6.86, 45.83), Double.NaN),
            GeolocationFix.Located(listOf(6.86, 45.83), Double.NaN),
        )
        assertEquals(
            GeolocationFix.Located(listOf(6.86, 45.83), Double.NaN).hashCode(),
            GeolocationFix.Located(listOf(6.86, 45.83), Double.NaN).hashCode(),
        )
        assertNotEquals<GeolocationFix>(
            GeolocationFix.Located(listOf(6.86, 45.83), 0.0),
            GeolocationFix.Located(listOf(6.86, 45.83), -0.0),
        )
        assertNotEquals<GeolocationFix>(
            GeolocationFix.Refused(GeolocationRefusal.Denied),
            GeolocationFix.Refused(GeolocationRefusal.Unavailable),
        )
        assertNotEquals<GeolocationFix>(
            GeolocationFix.Refused(GeolocationRefusal.NoApi),
            GeolocationFix.Located(emptyList(), 0.0),
        )
    }
}
