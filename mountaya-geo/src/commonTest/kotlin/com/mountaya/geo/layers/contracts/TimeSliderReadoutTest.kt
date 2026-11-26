package com.mountaya.geo.layers.contracts

import com.mountaya.geo.TimeSliderReadout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class TimeSliderReadoutTest {
    @Test
    fun twoReadoutsAreEqualWhereEveryMemberIs() {
        val at = Instant.fromEpochMilliseconds(1773484013900)
        val readout = TimeSliderReadout(at, "layers.timeHoursAhead", 1)

        assertEquals(readout, TimeSliderReadout(at, "layers.timeHoursAhead", 1))
        assertEquals(readout.hashCode(), TimeSliderReadout(at, "layers.timeHoursAhead", 1).hashCode())
        assertNotEquals(readout, TimeSliderReadout(null, "layers.timeHoursAhead", 1))
        assertNotEquals(readout, TimeSliderReadout(at, "layers.timeHoursAgo", 1))
        assertNotEquals(readout, TimeSliderReadout(at, "layers.timeHoursAhead", 2))
        assertNotEquals(TimeSliderReadout(at, "layers.timeNow", null), TimeSliderReadout(at, "layers.timeNow", -1))
    }
}
