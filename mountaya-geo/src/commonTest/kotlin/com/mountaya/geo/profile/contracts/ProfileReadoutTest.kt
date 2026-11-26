package com.mountaya.geo.profile.contracts

import com.mountaya.geo.ProfileClass
import com.mountaya.geo.ProfileReadout
import com.mountaya.geo.ReadoutCell
import com.mountaya.geo.ReadoutStat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProfileReadoutTest {
    @Test
    fun twoReadoutsAreEqualWhereTheirCellsGradeAndStretchAre() {
        val trail = ProfileClass(400.0, 600.0, "#e8590c", "Trail")
        val readout = ProfileReadout(listOf(ReadoutCell(ReadoutStat.Distance, 400.0)), 1.432, trail)

        assertEquals(ProfileReadout(listOf(ReadoutCell(ReadoutStat.Distance, 400.0)), 1.432, trail), readout)
        assertNotEquals(ProfileReadout(readout.cells, null, trail), readout)
        assertNotEquals(ProfileReadout(readout.cells, 1.432, null), readout)
        assertEquals(
            ProfileReadout(emptyList(), Double.NaN, null).hashCode(),
            ProfileReadout(emptyList(), Double.NaN, null).hashCode(),
        )
    }
}
