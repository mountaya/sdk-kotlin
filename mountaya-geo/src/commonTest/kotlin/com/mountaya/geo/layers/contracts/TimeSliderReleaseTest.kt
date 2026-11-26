package com.mountaya.geo.layers.contracts

import com.mountaya.geo.TimeSliderRelease
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class TimeSliderReleaseTest {
    @Test
    fun twoReleasesAreEqualWhereTheyPinTheSame() {
        val pin = Instant.parse("2026-03-14T15:26:53.900Z")

        assertEquals(TimeSliderRelease(pin), TimeSliderRelease(Instant.parse("2026-03-14T16:26:53.900+01:00")))
        assertEquals(TimeSliderRelease(null).hashCode(), TimeSliderRelease(null).hashCode())
        assertNotEquals(TimeSliderRelease(pin), TimeSliderRelease(null))
        assertNull(TimeSliderRelease(null).pin)
    }
}
