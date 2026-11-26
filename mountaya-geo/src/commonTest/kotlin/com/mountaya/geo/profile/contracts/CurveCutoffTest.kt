package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class CurveCutoffTest {
    @Test
    fun twoCutOffsAreEqualWhereEveryMemberIsTheInstantsAsOneNumberOfSeconds() {
        val gate = ProfilePlace(ProfileWaypoint.AtDistance("gate", 300.0), name = "Gate")
        val instant = Instant.fromEpochSeconds(1_788_000_000)
        val cutoff = CurveCutoff("gate#0", gate, 300.0, 60.0, instant)

        assertEquals(CurveCutoff("gate#0", gate, 300.0, 60.0, instant), cutoff)
        assertEquals(CurveCutoff("gate#0", gate, 300.0, 60.0, instant).hashCode(), cutoff.hashCode())
        assertNotEquals(CurveCutoff("gate#0", gate, 300.0, 60.0, instant + 1.seconds), cutoff)
    }
}
