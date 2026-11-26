package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ClimbTest {
    @Test
    fun twoClimbsAreEqualWhenEveryFigureAndTheCategoryAre() {
        val climb = Climb(0.8, 5.0, 1_240.0, 5.7, ClimbCategory.Category3)

        assertEquals(climb, Climb(0.8, 5.0, 1_240.0, 5.7, ClimbCategory.Category3))
        assertEquals(climb.hashCode(), Climb(0.8, 5.0, 1_240.0, 5.7, ClimbCategory.Category3).hashCode())
        assertNotEquals(climb, Climb(0.8, 5.0, 1_240.0, 5.7, ClimbCategory.Category2))
        assertEquals(
            "Climb(startKm=0.8, endKm=5.0, peakElevation=1240.0, avgGradePercent=5.7, category=Category3)",
            climb.toString(),
        )
    }
}
