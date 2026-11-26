package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClimbCategoryForTest {
    @Test
    fun theCategoryIsTheHardestTheScoreReaches() {
        assertEquals(ClimbCategory.Category4, climbCategoryFor(2_000.0, 4.0))
        assertEquals(ClimbCategory.Category3, climbCategoryFor(4_000.0, 4.0))
        assertEquals(ClimbCategory.Category2, climbCategoryFor(4_000.0, 8.0))
        assertEquals(ClimbCategory.Category1, climbCategoryFor(6_400.0, 10.0))
        assertEquals(ClimbCategory.HorsCategorie, climbCategoryFor(8_000.0, 10.0))
        assertEquals(ClimbCategory.Category4, climbCategoryFor(3_000.0, MIN_CLIMB_GRADE_PERCENT))
    }

    @Test
    fun underTheGradeFloorTheCategoryFloorOrNoNumberThereIsNone() {
        assertNull(climbCategoryFor(1_999.0, 4.0))
        assertNull(climbCategoryFor(50_000.0, 2.9))
        assertNull(climbCategoryFor(Double.NaN, 5.0))
        assertNull(climbCategoryFor(Double.POSITIVE_INFINITY, 5.0))
    }
}
