package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CurveDividerTest {
    @Test
    fun aDividerIsNamedAsItsPlaceIsAndEmptyForAPlaceWithNoName() {
        val col = ProfilePlace(ProfileWaypoint.AtDistance("col", 250.0), name = "Col")
        val marker = ProfilePlace(ProfileWaypoint.AtDistance("marker", 350.0), divides = true)

        assertEquals("Col", CurveDivider("col#0", col, 250.0, 50.0, 51.0).name)
        assertEquals("", CurveDivider("marker#0", marker, 350.0, 70.0, null).name)
    }

    @Test
    fun twoDividersAreEqualWhereEveryMemberIsTheSameAMissingHeightOnlyToAnother() {
        val col = ProfilePlace(ProfileWaypoint.AtDistance("col", 250.0), name = "Col")

        assertEquals(
            CurveDivider("col#0", col, 250.0, 50.0, null),
            CurveDivider("col#0", col, 250.0, 50.0, null),
        )
        assertNotEquals(
            CurveDivider("col#0", col, 250.0, 50.0, 51.0),
            CurveDivider("col#0", col, 250.0, 50.0, null),
        )
    }
}
