package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SectionGradeTest {
    @Test
    fun aLegsAverageGradeIsItsClimbLessItsLossOverItsLength() {
        val up = ItinerarySegmentRowMetrics(sectionDistance = 6_000.0, sectionAscent = 450.0, sectionDescent = 30.0)
        assertEquals(4.004172940709388, assertNotNull(sectionGrade(up), "the section has a grade"), 1e-12)
        val down = ItinerarySegmentRowMetrics(sectionDistance = 6_000.0, sectionAscent = 30.0, sectionDescent = 450.0)
        assertEquals(-4.004172940709388, assertNotNull(sectionGrade(down), "the section has a grade"), 1e-12)
    }

    @Test
    fun aMissingFigureOrNoLengthIsNoGrade() {
        assertNull(sectionGrade(ItinerarySegmentRowMetrics(sectionDistance = 6_000.0, sectionAscent = 450.0)))
        assertNull(
            sectionGrade(ItinerarySegmentRowMetrics(sectionDistance = 0.0, sectionAscent = 0.0, sectionDescent = 0.0)),
        )
        assertNull(
            sectionGrade(ItinerarySegmentRowMetrics(sectionDistance = -5.0, sectionAscent = 0.0, sectionDescent = 0.0)),
        )
    }
}
